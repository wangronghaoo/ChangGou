package com.changgou.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fescar.spring.annotation.GlobalTransactional;
import com.changgou.config.MqConfig;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.order.config.TokenDecode;
import com.changgou.order.dao.OrderItemMapper;
import com.changgou.order.dao.OrderLogMapper;
import com.changgou.order.dao.OrderMapper;
import com.changgou.order.dao.TaskMapper;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.pojo.OrderLog;
import com.changgou.order.pojo.Task;
import com.changgou.order.service.CartService;
import com.changgou.order.service.OrderService;
import com.changgou.order.pojo.Order;
import com.changgou.util.IdWorker;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;


    /**
     * 提供购物车数据
     */
    @Autowired
    private CartService cartService;

    /**
     * 分布式id
     */
    @Autowired
    private IdWorker idWorker;

    /**
     * 在数据库添加购物项
     */
    @Autowired
    private OrderItemMapper orderItemMapper;


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SkuFeign skuFeign;

    /**
     * 查询全部列表
     *
     * @return
     */
    @Override
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }

    /**
     * 根据ID查询
     *
     * @param id
     * @return
     */
    @Override
    public Order findById(String id) {
        return orderMapper.selectByPrimaryKey(id);
    }


    /**
     * 增加
     *
     * @param order
     */
    @Override
    public void add(Order order) {
        orderMapper.insert(order);
    }


    /**
     * 修改
     *
     * @param order
     */
    @Override
    public void update(Order order) {
        orderMapper.updateByPrimaryKey(order);
    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    public void delete(String id) {
        orderMapper.deleteByPrimaryKey(id);
    }


    /**
     * 条件查询
     *
     * @param searchMap
     * @return
     */
    @Override
    public List<Order> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return orderMapper.selectByExample(example);
    }

    /**
     * 分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Order> findPage(int page, int size) {
        PageHelper.startPage(page, size);
        return (Page<Order>) orderMapper.selectAll();
    }

    /**
     * 条件+分页查询
     *
     * @param searchMap 查询条件
     * @param page      页码
     * @param size      页大小
     * @return 分页结果
     */
    @Override
    public Page<Order> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page, size);
        Example example = createExample(searchMap);
        return (Page<Order>) orderMapper.selectByExample(example);
    }

    @Autowired
    private TaskMapper taskMapper;


    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 点击下单,进行对订单的生成,并且添加任务表,减库存添加积分
     * 如果订单超时未支付,发送到死信队列,进行对订单的关闭,以及回滚库存,添加日志,修改订单状态
     *
     * @param username
     * @param order
     * @return
     */
    @Override
    @GlobalTransactional(name = "order_saveOrder")
    public String saveOrder(String username, Order order) {

        //获取购物车数据
        Map<String, Object> map = cartService.cartList(username);
        List<OrderItem> orderItems = (List<OrderItem>) map.get("orderItems");

        Order saveOrder = new Order();
        saveOrder.setId(String.valueOf(idWorker.nextId()));
        saveOrder.setUsername(username);
        saveOrder.setReceiverAddress(order.getReceiverAddress());
        saveOrder.setReceiverContact(order.getReceiverContact());
        saveOrder.setReceiverMobile(order.getReceiverMobile());
        saveOrder.setPayType(order.getPayType());
        saveOrder.setTotalNum(Integer.parseInt(String.valueOf(map.get("totalNum"))));
        saveOrder.setTotalMoney(Integer.parseInt(String.valueOf(map.get("totalPrice"))));
        //给当前时间+8个小时
        Date date = new Date();
        long dateTime = date.getTime();
        dateTime += 1000 * 60 * 60 * 8;
        date.setTime(dateTime);
        saveOrder.setCreateTime(date);
        saveOrder.setUpdateTime(date);
        saveOrder.setIsDelete("0");
        saveOrder.setBuyerRate("0");  //未评价
        saveOrder.setSourceType("1");  //WEB
        saveOrder.setOrderStatus("0");  //0 未完成  1 已完成  2 已退货
        saveOrder.setPayStatus("0");    //0 未支付  1 已支付    2 支付失败
        saveOrder.setConsignStatus("0");  //0 未发货  1 已发货  2 已收货
        //分别添加订单数据

        orderMapper.insert(saveOrder);
        //添加购物项数据
        for (OrderItem orderItem : orderItems) {
            orderItem.setId(idWorker.nextId() + "");
            orderItem.setIsReturn("0");
            orderItem.setOrderId(saveOrder.getId());
            orderItemMapper.insert(orderItem);
        }


        //向任务表中填写数据
        Task task = new Task();
        task.setCreateTime(date);
        task.setUpdateTime(date);
        task.setMqExchange(MqConfig.AddPointExchange);
        task.setMqRoutingkey(MqConfig.AddPointRouteKey);
        Map taskMap = new HashMap();
        taskMap.put("orderId", saveOrder.getId());
        taskMap.put("username", saveOrder.getUsername());
        taskMap.put("point", saveOrder.getTotalMoney());

        String taskMessage = JSON.toJSONString(taskMap);
        task.setRequestBody(taskMessage);

        taskMapper.insertSelective(task);

        //删减库存
        skuFeign.updateCount(username);

//        抛出异常,购物项与订单不会写到数据库,但是库存会减少,tractional只会控制本地,而不会控制其他服务的事务管理
//        int i = 3/0;

        //删除redis中的购物车数据
        redisTemplate.delete("Cart_" + username);

        String orderId = saveOrder.getId();

        //将消息发送到死信队列
        rabbitTemplate.convertAndSend("", "queue.createOrder", saveOrder.getId());
        return orderId;


    }


    @Autowired
    private OrderLogMapper orderLogMapper;

    /**
     * 修改订单状态以及支付状态
     *
     * @param orderId
     * @param transactionId
     */
    @Override
    public void updateOrderStatus(String orderId, String transactionId) {

        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order != null && "0".equals(order.getPayStatus())) {
            //支付成功后修改订单信息
            order.setPayStatus("1");
            order.setOrderStatus("1");  //订单状态
            order.setUpdateTime(new Date());
            order.setTransactionId(transactionId);  //微信交易流水号
            order.setPayTime(new Date());

            orderMapper.updateByPrimaryKeySelective(order);
            //插入订单日志
            OrderLog orderLog = new OrderLog();
            orderLog.setId(idWorker.nextId() + "");
            orderLog.setOperater("admin");
            orderLog.setOperateTime(new Date());
            orderLog.setOrderId(orderId);
            orderLog.setOrderStatus("1");
            orderLog.setPayStatus("1");
            orderLog.setRemarks("交易流水号为:" + transactionId);
            orderLogMapper.insert(orderLog);

        }

    }

    /**
     * 未支付关闭订单
     *
     * @param deadOrderId
     */
    @Override
    public void updateOrder(String deadOrderId) {
        Order order = orderMapper.selectByPrimaryKey(deadOrderId);
        //未支付
        order.setOrderStatus("4");
        Date date = new Date();
        //+8个小时
        long time = date.getTime();
        time += 1000 * 60 * 60 * 8;
        date.setTime(time);
        order.setUpdateTime(date);
        order.setCloseTime(new Date());
        orderMapper.updateByPrimaryKeySelective(order);
        //记录订单日志
        OrderLog orderLog = new OrderLog();
        orderLog.setRemarks("未支付关闭订单");
        orderLog.setOrderStatus("4");
        orderLog.setOperater("admin");
        orderLog.setId(String.valueOf(idWorker.nextId()));
        orderLog.setOperateTime(date);
        orderLog.setOrderId(deadOrderId);
        orderLogMapper.insert(orderLog);
    }

    /**
     * 批量发货
     *
     * @param orders
     */
    @Override
    public Result send(List<Order> orders) {

        //判断订单是否存在或者运单号和物流公司是否为空
        for (Order order : orders) {
            if (order.getId() == null) {
                return new Result(false, StatusCode.ERROR,"该订单无效,重新选择");
            }

            if (order.getShippingCode() == null || order.getShippingName() == null) {
                return new Result(false,StatusCode.ERROR,"请填写物流单号和物流公司");
            }
        }
        //如果处于未支付,订单状态为0
        for (Order order : orders) {
            if ("0".equals(order.getPayStatus()) || "0".equals(order.getOrderStatus())){
                return new Result(false,StatusCode.ERROR,"订单状态异常");
            }
        }

        //对订单状态进行修改
        for (Order order : orders) {
            order.setUpdateTime(new Date());
            order.setConsignStatus("1");  //发货状态
            order.setOrderStatus("2");    //订单状态 : 2 已发货
            order.setShippingCode(order.getShippingCode());
            order.setShippingName(order.getShippingName());
            order.setConsignTime(new Date());  //发货时间

            orderMapper.updateByPrimaryKeySelective(order);

            //添加日志信息
            OrderLog orderLog = new OrderLog();
            orderLog.setId(String.valueOf(idWorker.nextId()));
            orderLog.setOperater("admin");
            orderLog.setOperateTime(new Date());
            orderLog.setOrderId(order.getId());
            orderLog.setOrderStatus("2");  //订单状态 0 ,1 已支付 2 已完成  3 已收货
            orderLog.setConsignStatus("1");  //发货状态  0:1
            orderLogMapper.insertSelective(orderLog);
        }
        return new Result(true,StatusCode.OK,"为您批量发货成功,共发货" + orders.size() + "件");
    }


    /**
     * 手动收货
     * @param orderId
     * @param operater
     */
    @Override
    public void confirmOrder(String orderId, String operater) {
        Order order = orderMapper.selectByPrimaryKey(orderId);

        if (!"1".equals(order.getConsignStatus())){
            throw new RuntimeException("该订单未发货" + orderId);
        }

        //修改订单相关数据
        order.setEndTime(new Date());
        order.setUpdateTime(new Date());
        order.setConsignStatus("2");
        order.setOrderStatus("3");
        orderMapper.updateByPrimaryKeySelective(order);

        //添加订单日志
        OrderLog orderLog = new OrderLog();
        orderLog.setId(String.valueOf(idWorker.nextId()));
        orderLog.setConsignStatus("2");
        orderLog.setOrderStatus("3");
        orderLog.setOrderId(order.getId());
        orderLog.setOperater(operater);  //收货人/用户/系统
        orderLog.setOperateTime(new Date());
        orderLogMapper.insertSelective(orderLog);
    }


    /**
     * 构建查询对象
     *
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap) {
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if (searchMap != null) {
            // 订单id
            if (searchMap.get("id") != null && !"".equals(searchMap.get("id"))) {
                criteria.andEqualTo("id", searchMap.get("id"));
            }
            // 支付类型，1、在线支付、0 货到付款
            if (searchMap.get("payType") != null && !"".equals(searchMap.get("payType"))) {
                criteria.andEqualTo("payType", searchMap.get("payType"));
            }
            // 物流名称
            if (searchMap.get("shippingName") != null && !"".equals(searchMap.get("shippingName"))) {
                criteria.andLike("shippingName", "%" + searchMap.get("shippingName") + "%");
            }
            // 物流单号
            if (searchMap.get("shippingCode") != null && !"".equals(searchMap.get("shippingCode"))) {
                criteria.andLike("shippingCode", "%" + searchMap.get("shippingCode") + "%");
            }
            // 用户名称
            if (searchMap.get("username") != null && !"".equals(searchMap.get("username"))) {
                criteria.andLike("username", "%" + searchMap.get("username") + "%");
            }
            // 买家留言
            if (searchMap.get("buyerMessage") != null && !"".equals(searchMap.get("buyerMessage"))) {
                criteria.andLike("buyerMessage", "%" + searchMap.get("buyerMessage") + "%");
            }
            // 是否评价
            if (searchMap.get("buyerRate") != null && !"".equals(searchMap.get("buyerRate"))) {
                criteria.andLike("buyerRate", "%" + searchMap.get("buyerRate") + "%");
            }
            // 收货人
            if (searchMap.get("receiverContact") != null && !"".equals(searchMap.get("receiverContact"))) {
                criteria.andLike("receiverContact", "%" + searchMap.get("receiverContact") + "%");
            }
            // 收货人手机
            if (searchMap.get("receiverMobile") != null && !"".equals(searchMap.get("receiverMobile"))) {
                criteria.andLike("receiverMobile", "%" + searchMap.get("receiverMobile") + "%");
            }
            // 收货人地址
            if (searchMap.get("receiverAddress") != null && !"".equals(searchMap.get("receiverAddress"))) {
                criteria.andLike("receiverAddress", "%" + searchMap.get("receiverAddress") + "%");
            }
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if (searchMap.get("sourceType") != null && !"".equals(searchMap.get("sourceType"))) {
                criteria.andEqualTo("sourceType", searchMap.get("sourceType"));
            }
            // 交易流水号
            if (searchMap.get("transactionId") != null && !"".equals(searchMap.get("transactionId"))) {
                criteria.andLike("transactionId", "%" + searchMap.get("transactionId") + "%");
            }
            // 订单状态
            if (searchMap.get("orderStatus") != null && !"".equals(searchMap.get("orderStatus"))) {
                criteria.andEqualTo("orderStatus", searchMap.get("orderStatus"));
            }
            // 支付状态
            if (searchMap.get("payStatus") != null && !"".equals(searchMap.get("payStatus"))) {
                criteria.andEqualTo("payStatus", searchMap.get("payStatus"));
            }
            // 发货状态
            if (searchMap.get("consignStatus") != null && !"".equals(searchMap.get("consignStatus"))) {
                criteria.andEqualTo("consignStatus", searchMap.get("consignStatus"));
            }
            // 是否删除
            if (searchMap.get("isDelete") != null && !"".equals(searchMap.get("isDelete"))) {
                criteria.andEqualTo("isDelete", searchMap.get("isDelete"));
            }

            // 数量合计
            if (searchMap.get("totalNum") != null) {
                criteria.andEqualTo("totalNum", searchMap.get("totalNum"));
            }
            // 金额合计
            if (searchMap.get("totalMoney") != null) {
                criteria.andEqualTo("totalMoney", searchMap.get("totalMoney"));
            }
            // 优惠金额
            if (searchMap.get("preMoney") != null) {
                criteria.andEqualTo("preMoney", searchMap.get("preMoney"));
            }
            // 邮费
            if (searchMap.get("postFee") != null) {
                criteria.andEqualTo("postFee", searchMap.get("postFee"));
            }
            // 实付金额
            if (searchMap.get("payMoney") != null) {
                criteria.andEqualTo("payMoney", searchMap.get("payMoney"));
            }

        }
        return example;
    }


}
