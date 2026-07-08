package com.campus.controller;

import com.campus.common.Result;
import com.campus.mapper.GoodsInfoMapper;
import com.campus.mapper.GoodsOrderMapper;
import com.campus.mapper.PointMapper;
import com.campus.mapper.SysUserMapper;
import com.campus.pojo.GoodsQueryDTO;
import com.campus.pojo.dto.AdminDashboardStatsDTO;
import com.campus.pojo.dto.AdminDashboardTrendDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@Api(tags = "管理员-仪表盘")
@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin
public class AdminDashboardController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private GoodsInfoMapper goodsInfoMapper;

    @Autowired
    private GoodsOrderMapper goodsOrderMapper;

    @Autowired
    private PointMapper pointMapper;

    @ApiOperation("统计数据")
    @GetMapping("/stats")
    public Result<AdminDashboardStatsDTO> stats() {
        AdminDashboardStatsDTO dto = new AdminDashboardStatsDTO();
        dto.setUserTotal(sysUserMapper.countAdminList(null, null));
        dto.setUserToday(sysUserMapper.countTodayNew());
        GoodsQueryDTO goodsQueryDTO = new GoodsQueryDTO();
        goodsQueryDTO.setStatus(-1);
        goodsQueryDTO.setPageNum(1);
        goodsQueryDTO.setPageSize(1);
        dto.setGoodsTotal(goodsInfoMapper.countList(goodsQueryDTO));
        dto.setGoodsOnSale(goodsInfoMapper.countOnSale());
        dto.setGoodsToday(goodsInfoMapper.countTodayNew());
        dto.setOrderTotal(goodsOrderMapper.countAdminList(null, null, null));
        dto.setOrderToday(goodsOrderMapper.countTodayNew());
        dto.setOrderCompletedTotal(goodsOrderMapper.countCompletedTotal());
        dto.setOrderCompletedToday(goodsOrderMapper.countCompletedToday());
        dto.setGmvTotal(goodsOrderMapper.sumGmvTotal());
        dto.setGmvToday(goodsOrderMapper.sumGmvToday());
        dto.setPointGoodsTotal(pointMapper.countAdminPointGoods(null, null));
        dto.setPointOrderTotal(pointMapper.countAdminOrders(null, null));
        dto.setPointOrderToday(pointMapper.countTodayOrders());
        return Result.success(dto);
    }

    @ApiOperation("趋势数据（近N天）")
    @GetMapping("/trend")
    public Result<List<AdminDashboardTrendDTO>> trend(@RequestParam(defaultValue = "7") Integer days) {
        if (days == null || days <= 0) {
            days = 7;
        }
        if (days > 30) {
            days = 30;
        }
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1L);
        String startDate = start.toString();
        String endDate = end.toString();

        List<AdminDashboardTrendDTO> orders = goodsOrderMapper.selectOrderTrend(startDate, endDate);
        List<AdminDashboardTrendDTO> users = sysUserMapper.selectDailyNewUsers(startDate, endDate);
        List<AdminDashboardTrendDTO> goods = goodsInfoMapper.selectDailyNewGoods(startDate, endDate);

        Map<String, AdminDashboardTrendDTO> map = new LinkedHashMap<>();
        for (int i = 0; i < days; i++) {
            String date = start.plusDays(i).toString();
            AdminDashboardTrendDTO dto = new AdminDashboardTrendDTO();
            dto.setDate(date);
            dto.setOrderCount(0);
            dto.setGmv(java.math.BigDecimal.ZERO);
            dto.setNewUsers(0);
            dto.setNewGoods(0);
            map.put(date, dto);
        }
        if (orders != null) {
            for (AdminDashboardTrendDTO o : orders) {
                AdminDashboardTrendDTO dto = map.get(o.getDate());
                if (dto != null) {
                    dto.setOrderCount(o.getOrderCount());
                    dto.setGmv(o.getGmv() == null ? java.math.BigDecimal.ZERO : o.getGmv());
                }
            }
        }
        if (users != null) {
            for (AdminDashboardTrendDTO u : users) {
                AdminDashboardTrendDTO dto = map.get(u.getDate());
                if (dto != null) {
                    dto.setNewUsers(u.getNewUsers());
                }
            }
        }
        if (goods != null) {
            for (AdminDashboardTrendDTO g : goods) {
                AdminDashboardTrendDTO dto = map.get(g.getDate());
                if (dto != null) {
                    dto.setNewGoods(g.getNewGoods());
                }
            }
        }
        return Result.success(new ArrayList<>(map.values()));
    }
}
