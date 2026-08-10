package com.campus.service.impl;

import com.campus.exception.GlobalException;
import com.campus.mapper.GoodsInfoMapper;
import com.campus.mapper.GoodsOrderMapper;
import com.campus.pojo.CreateOrderDTO;
import com.campus.pojo.GoodsInfo;
import com.campus.pojo.GoodsOrder;
import com.campus.pojo.PageResult;
import com.campus.mapper.PointMapper;
import com.campus.mapper.SysUserMapper;
import com.campus.pojo.PointRecord;
import com.campus.pojo.SysUser;
import com.campus.pojo.GoodsStatus;
import com.campus.pojo.TradeType;
import com.campus.pojo.OrderStatus;
import com.campus.service.GoodsOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class GoodsOrderServiceImpl implements GoodsOrderService {

    @Autowired
    private GoodsOrderMapper goodsOrderMapper;

    @Autowired
    private GoodsInfoMapper goodsInfoMapper;

    @Autowired
    private PointMapper pointMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GoodsOrder createOrder(CreateOrderDTO dto, Long userId) {
        GoodsInfo targetGoods = goodsInfoMapper.selectById(dto.getGoodsId());
        if (targetGoods == null || targetGoods.getStatus() != GoodsStatus.ON_SALE.getValue()) {
            throw new GlobalException("该商品不存在或已下架/售出");
        }
        if (targetGoods.getUserId().equals(userId)) {
            throw new GlobalException("不能购买/置换自己发布的商品");
        }

        GoodsOrder order = new GoodsOrder();
        order.setOrderNo("OD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        order.setBuyerId(userId);
        order.setSellerId(targetGoods.getUserId());
        order.setGoodsId(targetGoods.getGoodsId());
        order.setTradeType(dto.getTradeType());
        order.setCreateTime(LocalDateTime.now());

        if (dto.getTradeType() != null && dto.getTradeType() == TradeType.BARTER.getValue()) {
            if (targetGoods.getIsExchange() != 1) {
                throw new GlobalException("该商品不支持换物");
            }
            if (dto.getExchangeGoodsId() == null) {
                throw new GlobalException("请选择您用来交换的商品");
            }
            GoodsInfo myGoods = goodsInfoMapper.selectById(dto.getExchangeGoodsId());
            if (myGoods == null || !myGoods.getUserId().equals(userId)) {
                throw new GlobalException("您选择的交换商品无效");
            }
            // 乐观锁：仅当交换商品仍在售时才锁定
            int casRows = goodsInfoMapper.updateStatusCas(myGoods.getGoodsId(), GoodsStatus.BARTER_PENDING.getValue(), GoodsStatus.ON_SALE.getValue());
            if (casRows == 0) {
                throw new GlobalException("交换商品状态已变更，请刷新后重试");
            }
            order.setAmount(BigDecimal.ZERO);
            order.setExchangeGoodsId(myGoods.getGoodsId());
            order.setStatus(OrderStatus.BARTER_PENDING.getValue()); // 0-待卖家同意
        } else {
            // 普通购买 - 乐观锁：仅当商品仍在售时才标记为已售出
            int casRows = goodsInfoMapper.updateStatusCas(targetGoods.getGoodsId(), GoodsStatus.SOLD.getValue(), GoodsStatus.ON_SALE.getValue());
            if (casRows == 0) {
                throw new GlobalException("商品已被其他用户购买，请刷新后重试");
            }
            order.setAmount(targetGoods.getPrice());
            order.setStatus(OrderStatus.WAIT_RECEIVE.getValue()); // 1-待收货

            // 级联取消其它与该商品关联的待同意订单，并解锁那些订单对应的交换商品
            cascadeCancelPendingForGoods(targetGoods.getGoodsId(), null);
        }

        goodsOrderMapper.insert(order);
        return order;
    }

    @Override
    public List<GoodsOrder> getMyBuyOrders(Long userId) {
        return goodsOrderMapper.selectByUserId(userId, "buy");
    }

    @Override
    public List<GoodsOrder> getMySellOrders(Long userId) {
        return goodsOrderMapper.selectByUserId(userId, "sell");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processOrder(Long orderId, Integer status, Long userId) {
        GoodsOrder order = goodsOrderMapper.selectById(orderId);
        if (order == null) {
            throw new GlobalException("订单不存在");
        }
        // 卖家处理换物请求: 1-同意 (WAIT_RECEIVE), 4-拒绝 (BARTER_REJECTED)
        if (order.getTradeType() == TradeType.BARTER.getValue() && order.getStatus() == OrderStatus.BARTER_PENDING.getValue()) {
            if (!order.getSellerId().equals(userId)) {
                throw new GlobalException("无权操作此订单");
            }
            if (status == OrderStatus.WAIT_RECEIVE.getValue()) {
                GoodsInfo targetGoods = goodsInfoMapper.selectById(order.getGoodsId());
                GoodsInfo exchangeGoods = goodsInfoMapper.selectById(order.getExchangeGoodsId());
                boolean targetUnavailable = (targetGoods == null || targetGoods.getStatus() != GoodsStatus.ON_SALE.getValue());
                boolean exchangeUnavailable = (exchangeGoods == null || exchangeGoods.getStatus() != GoodsStatus.BARTER_PENDING.getValue());
                if (targetUnavailable || exchangeUnavailable) {
                    goodsOrderMapper.updateStatusCas(orderId, OrderStatus.CANCELLED.getValue(), OrderStatus.BARTER_PENDING.getValue());
                    if (exchangeGoods != null && exchangeGoods.getStatus() == GoodsStatus.BARTER_PENDING.getValue()) {
                        goodsInfoMapper.updateStatusCas(exchangeGoods.getGoodsId(), GoodsStatus.ON_SALE.getValue(), GoodsStatus.BARTER_PENDING.getValue());
                    }
                    String msg;
                    if (targetUnavailable && exchangeUnavailable) {
                        msg = "交换物品和被换物品已售出，换物订单已取消";
                    } else if (exchangeUnavailable) {
                        msg = "交换物品已售出，换物订单已取消";
                    } else {
                        msg = "被换物品已售出，换物订单已取消";
                    }
                    throw new GlobalException(msg);
                }
                // 订单与商品状态迁移全部走后置条件校验（CAS），杜绝并发覆盖
                int orderRows = goodsOrderMapper.updateStatusCas(orderId, OrderStatus.WAIT_RECEIVE.getValue(), OrderStatus.BARTER_PENDING.getValue());
                int targetRows = goodsInfoMapper.updateStatusCas(order.getGoodsId(), GoodsStatus.SOLD.getValue(), GoodsStatus.ON_SALE.getValue());
                int exchangeRows = goodsInfoMapper.updateStatusCas(order.getExchangeGoodsId(), GoodsStatus.SOLD.getValue(), GoodsStatus.BARTER_PENDING.getValue());
                if (orderRows == 0 || targetRows == 0 || exchangeRows == 0) {
                    throw new GlobalException("换物商品或订单状态已变更，请刷新后重试");
                }

                // 级联取消其它与这两个商品关联的待同意订单，并解锁它们对应的交换商品
                cascadeCancelPendingForGoods(order.getGoodsId(), orderId);
                cascadeCancelPendingForGoods(order.getExchangeGoodsId(), orderId);
            } else if (status == OrderStatus.CANCELLED.getValue() || status == OrderStatus.BARTER_REJECTED.getValue()) {
                int rows = goodsOrderMapper.updateStatusCas(orderId, OrderStatus.BARTER_REJECTED.getValue(), OrderStatus.BARTER_PENDING.getValue());
                if (rows == 0) {
                    throw new GlobalException("订单状态已变更，请刷新后重试");
                }
                goodsInfoMapper.updateStatusCas(order.getExchangeGoodsId(), GoodsStatus.ON_SALE.getValue(), GoodsStatus.BARTER_PENDING.getValue());
            } else {
                throw new GlobalException("当前订单状态不支持此操作");
            }
        } else if (status == OrderStatus.COMPLETED.getValue()) {
            if (!order.getBuyerId().equals(userId)) {
                throw new GlobalException("无权操作");
            }
            int rows = goodsOrderMapper.updateStatusCas(orderId, OrderStatus.COMPLETED.getValue(), OrderStatus.WAIT_RECEIVE.getValue());
            if (rows == 0) {
                throw new GlobalException("订单状态已变更，无法重复确认收货");
            }

            int points = calcTradeRewardPoints(order);
            if (points > 0) {
                rewardPointsForTrade(order.getBuyerId(), points);
                rewardPointsForTrade(order.getSellerId(), points);
            }
        } else {
            throw new GlobalException("当前订单状态不支持此操作");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long userId) {
        GoodsOrder order = goodsOrderMapper.selectById(orderId);
        if (order == null) {
            throw new GlobalException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            throw new GlobalException("无权操作");
        }
        if (order.getTradeType() == TradeType.BUY.getValue()) {
            if (order.getStatus() != OrderStatus.WAIT_RECEIVE.getValue()) {
                throw new GlobalException("当前订单状态不支持取消");
            }
            GoodsInfo goods = goodsInfoMapper.selectById(order.getGoodsId());
            if (goods == null) {
                throw new GlobalException("商品不存在");
            }
            if (goods.getStatus() != GoodsStatus.SOLD.getValue()) {
                throw new GlobalException("商品状态异常，无法取消订单");
            }
            int orderRows = goodsOrderMapper.updateStatusCas(orderId, OrderStatus.CANCELLED.getValue(), OrderStatus.WAIT_RECEIVE.getValue());
            if (orderRows == 0) {
                throw new GlobalException("订单状态已变更，无法取消");
            }
            goodsInfoMapper.updateStatusCas(order.getGoodsId(), GoodsStatus.ON_SALE.getValue(), GoodsStatus.SOLD.getValue());
            return;
        }
        if (order.getTradeType() == TradeType.BARTER.getValue()) {
            if (order.getStatus() != OrderStatus.BARTER_PENDING.getValue()) {
                throw new GlobalException("当前换物订单状态不支持取消");
            }
            int orderRows = goodsOrderMapper.updateStatusCas(orderId, OrderStatus.CANCELLED.getValue(), OrderStatus.BARTER_PENDING.getValue());
            if (orderRows == 0) {
                throw new GlobalException("订单状态已变更，无法取消");
            }
            goodsInfoMapper.updateStatusCas(order.getExchangeGoodsId(), GoodsStatus.ON_SALE.getValue(), GoodsStatus.BARTER_PENDING.getValue());
            return;
        }
        throw new GlobalException("不支持的订单类型");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrderBySeller(Long orderId, Long sellerId) {
        GoodsOrder order = goodsOrderMapper.selectById(orderId);
        if (order == null) {
            throw new GlobalException("订单不存在");
        }
        if (!order.getSellerId().equals(sellerId)) {
            throw new GlobalException("无权操作");
        }
        if (order.getTradeType() == TradeType.BUY.getValue()) {
            if (order.getStatus() != OrderStatus.WAIT_RECEIVE.getValue()) {
                throw new GlobalException("当前订单状态不支持取消");
            }
            GoodsInfo goods = goodsInfoMapper.selectById(order.getGoodsId());
            if (goods == null) {
                throw new GlobalException("商品不存在");
            }
            if (goods.getStatus() != GoodsStatus.SOLD.getValue()) {
                throw new GlobalException("商品状态异常，无法取消订单");
            }
            int orderRows = goodsOrderMapper.updateStatusCas(orderId, OrderStatus.CANCELLED.getValue(), OrderStatus.WAIT_RECEIVE.getValue());
            if (orderRows == 0) {
                throw new GlobalException("订单状态已变更，无法取消");
            }
            goodsInfoMapper.updateStatusCas(order.getGoodsId(), GoodsStatus.ON_SALE.getValue(), GoodsStatus.SOLD.getValue());
            return;
        }
        if (order.getTradeType() == TradeType.BARTER.getValue()) {
            if (order.getStatus() != OrderStatus.BARTER_PENDING.getValue() && order.getStatus() != OrderStatus.WAIT_RECEIVE.getValue()) {
                throw new GlobalException("当前换物订单状态不支持取消");
            }
            Integer prevStatus = order.getStatus();
            if (prevStatus == OrderStatus.BARTER_PENDING.getValue()) {
                int orderRows = goodsOrderMapper.updateStatusCas(orderId, OrderStatus.CANCELLED.getValue(), OrderStatus.BARTER_PENDING.getValue());
                if (orderRows == 0) {
                    throw new GlobalException("订单状态已变更，无法取消");
                }
                if (order.getExchangeGoodsId() != null) {
                    goodsInfoMapper.updateStatusCas(order.getExchangeGoodsId(), GoodsStatus.ON_SALE.getValue(), GoodsStatus.BARTER_PENDING.getValue());
                }
            } else if (prevStatus == OrderStatus.WAIT_RECEIVE.getValue()) {
                int orderRows = goodsOrderMapper.updateStatusCas(orderId, OrderStatus.CANCELLED.getValue(), OrderStatus.WAIT_RECEIVE.getValue());
                if (orderRows == 0) {
                    throw new GlobalException("订单状态已变更，无法取消");
                }
                if (order.getGoodsId() != null) {
                    goodsInfoMapper.updateStatusCas(order.getGoodsId(), GoodsStatus.ON_SALE.getValue(), GoodsStatus.SOLD.getValue());
                }
                if (order.getExchangeGoodsId() != null) {
                    goodsInfoMapper.updateStatusCas(order.getExchangeGoodsId(), GoodsStatus.ON_SALE.getValue(), GoodsStatus.SOLD.getValue());
                }
            }
            return;
        }
        throw new GlobalException("不支持的订单类型");
    }

    private void rewardPointsForTrade(Long userId, int points) {
        pointMapper.updateUserPoints(userId, points);
        SysUser user = sysUserMapper.selectById(userId);

        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setChangeType(1); // 1-交易获取
        record.setChangeAmount(points);
        record.setBalanceAfter(user.getPoints());
        record.setCreateTime(LocalDateTime.now());
        pointMapper.insertRecord(record);
    }

    private int calcTradeRewardPoints(GoodsOrder order) {
        if (order == null) {
            return 0;
        }
        if (order.getTradeType() == TradeType.BUY.getValue()) {
            BigDecimal amount = order.getAmount();
            if (amount == null) {
                return 0;
            }
            return amount.multiply(new BigDecimal("0.10")).setScale(0, RoundingMode.DOWN).intValue();
        }
        if (order.getTradeType() == TradeType.BARTER.getValue()) {
            GoodsInfo g1 = goodsInfoMapper.selectById(order.getGoodsId());
            GoodsInfo g2 = goodsInfoMapper.selectById(order.getExchangeGoodsId());
            if (g1 == null || g2 == null || g1.getPrice() == null || g2.getPrice() == null) {
                return 0;
            }
            BigDecimal avg = g1.getPrice().add(g2.getPrice()).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
            return avg.multiply(new BigDecimal("0.10")).setScale(0, RoundingMode.DOWN).intValue();
        }
        return 0;
    }

    /**
     * 级联取消指定商品相关的待同意/已同意换物订单，并将对应交换商品解锁回在售状态。
     * 使用 CAS 仅释放仍处于锁定状态的交换商品，避免误覆盖其他并发操作。
     */
    private void cascadeCancelPendingForGoods(Long goodsId, Long excludeOrderId) {
        List<GoodsOrder> pendingOrders;
        if (excludeOrderId != null) {
            pendingOrders = goodsOrderMapper.selectPendingExchangeOrdersByGoodsIdExcludeOrderId(goodsId, excludeOrderId);
        } else {
            pendingOrders = goodsOrderMapper.selectPendingExchangeOrdersByGoodsId(goodsId);
        }
        for (GoodsOrder pendingOrder : pendingOrders) {
            if (pendingOrder.getExchangeGoodsId() != null) {
                goodsInfoMapper.updateStatusCas(pendingOrder.getExchangeGoodsId(), GoodsStatus.ON_SALE.getValue(), GoodsStatus.BARTER_PENDING.getValue());
            }
        }
        if (excludeOrderId != null) {
            goodsOrderMapper.cancelPendingExchangeOrdersByGoodsIdExcludeOrderId(goodsId, excludeOrderId);
        } else {
            goodsOrderMapper.cancelPendingExchangeOrdersByGoodsId(goodsId);
        }
    }

    @Override
    public PageResult<GoodsOrder> getAdminOrderList(int pageNum, int pageSize, String keyword, Integer tradeType, Integer status) {
        // 分页参数边界校验
        pageNum = Math.max(1, pageNum);
        pageSize = Math.min(100, Math.max(1, pageSize));
        int offset = (pageNum - 1) * pageSize;
        List<GoodsOrder> list = goodsOrderMapper.selectAdminList(offset, pageSize, keyword, tradeType, status);
        long total = goodsOrderMapper.countAdminList(keyword, tradeType, status);
        return new PageResult<>(total, list);
    }

    @Override
    public GoodsOrder getAdminOrderDetail(Long orderId) {
        GoodsOrder order = goodsOrderMapper.selectById(orderId);
        if (order == null) {
            throw new GlobalException("订单不存在");
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceCancelByAdmin(Long orderId) {
        GoodsOrder order = goodsOrderMapper.selectById(orderId);
        if (order == null) {
            throw new GlobalException("订单不存在");
        }
        if (order.getTradeType() == TradeType.BUY.getValue()) {
            if (order.getStatus() != OrderStatus.WAIT_RECEIVE.getValue()) {
                throw new GlobalException("当前订单状态不支持取消");
            }
            int orderRows = goodsOrderMapper.updateStatusCas(orderId, OrderStatus.CANCELLED.getValue(), OrderStatus.WAIT_RECEIVE.getValue());
            if (orderRows == 0) {
                throw new GlobalException("订单状态已变更，无法取消");
            }
            goodsInfoMapper.updateStatusCas(order.getGoodsId(), GoodsStatus.ON_SALE.getValue(), GoodsStatus.SOLD.getValue());
            return;
        }
        if (order.getTradeType() == TradeType.BARTER.getValue()) {
            if (order.getStatus() == OrderStatus.COMPLETED.getValue()) {
                throw new GlobalException("已完成订单不可取消");
            }
            if (order.getStatus() == OrderStatus.BARTER_REJECTED.getValue()) {
                throw new GlobalException("已拒绝订单不可取消");
            }
            if (order.getStatus() == OrderStatus.CANCELLED.getValue()) {
                throw new GlobalException("订单已取消");
            }
            if (order.getStatus() == OrderStatus.BARTER_PENDING.getValue()) {
                int orderRows = goodsOrderMapper.updateStatusCas(orderId, OrderStatus.CANCELLED.getValue(), OrderStatus.BARTER_PENDING.getValue());
                if (orderRows == 0) {
                    throw new GlobalException("订单状态已变更，无法取消");
                }
                goodsInfoMapper.updateStatusCas(order.getExchangeGoodsId(), GoodsStatus.ON_SALE.getValue(), GoodsStatus.BARTER_PENDING.getValue());
                return;
            }
            if (order.getStatus() == OrderStatus.WAIT_RECEIVE.getValue()) {
                int orderRows = goodsOrderMapper.updateStatusCas(orderId, OrderStatus.CANCELLED.getValue(), OrderStatus.WAIT_RECEIVE.getValue());
                if (orderRows == 0) {
                    throw new GlobalException("订单状态已变更，无法取消");
                }
                if (order.getGoodsId() != null) {
                    goodsInfoMapper.updateStatusCas(order.getGoodsId(), GoodsStatus.ON_SALE.getValue(), GoodsStatus.SOLD.getValue());
                }
                if (order.getExchangeGoodsId() != null) {
                    goodsInfoMapper.updateStatusCas(order.getExchangeGoodsId(), GoodsStatus.ON_SALE.getValue(), GoodsStatus.SOLD.getValue());
                }
                return;
            }
        }
        throw new GlobalException("不支持的订单类型");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceCompleteByAdmin(Long orderId) {
        GoodsOrder order = goodsOrderMapper.selectById(orderId);
        if (order == null) {
            throw new GlobalException("订单不存在");
        }
        if (order.getStatus() == OrderStatus.COMPLETED.getValue()) {
            throw new GlobalException("订单已完成");
        }
        if (order.getStatus() == OrderStatus.CANCELLED.getValue()) {
            throw new GlobalException("订单已取消");
        }
        if (order.getStatus() == OrderStatus.BARTER_REJECTED.getValue()) {
            throw new GlobalException("订单已拒绝");
        }
        if (order.getTradeType() == TradeType.BUY.getValue()) {
            if (order.getStatus() != OrderStatus.WAIT_RECEIVE.getValue()) {
                throw new GlobalException("当前订单状态不支持完成");
            }
            int orderRows = goodsOrderMapper.updateStatusCas(orderId, OrderStatus.COMPLETED.getValue(), OrderStatus.WAIT_RECEIVE.getValue());
            if (orderRows == 0) {
                throw new GlobalException("订单状态已变更，无法完成");
            }
            int points = calcTradeRewardPoints(order);
            if (points > 0) {
                rewardPointsForTrade(order.getBuyerId(), points);
                rewardPointsForTrade(order.getSellerId(), points);
            }
            return;
        }
        if (order.getTradeType() == TradeType.BARTER.getValue()) {
            if (order.getStatus() == OrderStatus.BARTER_PENDING.getValue()) {
                GoodsInfo targetGoods = goodsInfoMapper.selectById(order.getGoodsId());
                GoodsInfo exchangeGoods = goodsInfoMapper.selectById(order.getExchangeGoodsId());
                boolean targetUnavailable = (targetGoods == null || targetGoods.getStatus() != GoodsStatus.ON_SALE.getValue());
                boolean exchangeUnavailable = (exchangeGoods == null || exchangeGoods.getStatus() != GoodsStatus.BARTER_PENDING.getValue());
                if (targetUnavailable || exchangeUnavailable) {
                    goodsOrderMapper.updateStatusCas(orderId, OrderStatus.CANCELLED.getValue(), OrderStatus.BARTER_PENDING.getValue());
                    if (exchangeGoods != null && exchangeGoods.getStatus() == GoodsStatus.BARTER_PENDING.getValue()) {
                        goodsInfoMapper.updateStatusCas(exchangeGoods.getGoodsId(), GoodsStatus.ON_SALE.getValue(), GoodsStatus.BARTER_PENDING.getValue());
                    }
                    throw new GlobalException("换物商品状态异常，订单已取消");
                }
                int targetRows = goodsInfoMapper.updateStatusCas(order.getGoodsId(), GoodsStatus.SOLD.getValue(), GoodsStatus.ON_SALE.getValue());
                int exchangeRows = goodsInfoMapper.updateStatusCas(order.getExchangeGoodsId(), GoodsStatus.SOLD.getValue(), GoodsStatus.BARTER_PENDING.getValue());
                if (targetRows == 0 || exchangeRows == 0) {
                    throw new GlobalException("换物商品状态已变更，请刷新后重试");
                }

                cascadeCancelPendingForGoods(order.getGoodsId(), orderId);
                cascadeCancelPendingForGoods(order.getExchangeGoodsId(), orderId);

                int orderRows = goodsOrderMapper.updateStatusCas(orderId, OrderStatus.COMPLETED.getValue(), OrderStatus.BARTER_PENDING.getValue());
                if (orderRows == 0) {
                    throw new GlobalException("订单状态已变更，无法完成");
                }
                int points = calcTradeRewardPoints(order);
                if (points > 0) {
                    rewardPointsForTrade(order.getBuyerId(), points);
                    rewardPointsForTrade(order.getSellerId(), points);
                }
                return;
            }
            if (order.getStatus() != OrderStatus.WAIT_RECEIVE.getValue()) {
                throw new GlobalException("当前订单状态不支持完成");
            }
            int orderRows = goodsOrderMapper.updateStatusCas(orderId, OrderStatus.COMPLETED.getValue(), OrderStatus.WAIT_RECEIVE.getValue());
            if (orderRows == 0) {
                throw new GlobalException("订单状态已变更，无法完成");
            }
            int points = calcTradeRewardPoints(order);
            if (points > 0) {
                rewardPointsForTrade(order.getBuyerId(), points);
                rewardPointsForTrade(order.getSellerId(), points);
            }
            return;
        }
        throw new GlobalException("不支持的订单类型");
    }
}