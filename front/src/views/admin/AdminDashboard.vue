<template>
  <div class="admin-dashboard">
    <div class="title">仪表盘</div>
    <div class="stat-section">
      <el-row :gutter="12">
        <el-col :xs="24" :sm="12" :md="8" :lg="8">
          <el-card class="stat-card" v-loading="loading">
            <div class="stat-label">用户总数</div>
            <div class="stat-value">{{ stats.userTotal }}</div>
            <div class="stat-sub">
              <span>今日新增</span>
              <span>+{{ stats.userToday }}</span>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="8">
          <el-card class="stat-card" v-loading="loading">
            <div class="stat-label">商品总数</div>
            <div class="stat-value">{{ stats.goodsTotal }}</div>
            <div class="stat-sub">
              <span>今日新增</span>
              <span>+{{ stats.goodsToday }}</span>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="8">
          <el-card class="stat-card" v-loading="loading">
            <div class="stat-label">在售商品数</div>
            <div class="stat-value">{{ stats.goodsOnSale }}</div>
            <div class="stat-sub">&nbsp;</div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="8">
          <el-card class="stat-card" v-loading="loading">
            <div class="stat-label">成交额（已完成）</div>
            <div class="stat-value">￥{{ formatMoney(stats.gmvTotal) }}</div>
            <div class="stat-sub">
              <span>今日</span>
              <span>￥{{ formatMoney(stats.gmvToday) }}</span>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="8">
          <el-card class="stat-card" v-loading="loading">
            <div class="stat-label">积分订单</div>
            <div class="stat-value">{{ stats.pointOrderTotal }}</div>
            <div class="stat-sub">
              <span>今日新增</span>
              <span>+{{ stats.pointOrderToday }}</span>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="8">
          <el-card class="stat-card" v-loading="loading">
            <div class="stat-label">积分商品</div>
            <div class="stat-value">{{ stats.pointGoodsTotal }}</div>
            <div class="stat-sub">&nbsp;</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="chart-section">
      <el-row :gutter="12">
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card" v-loading="loadingTrend">
            <div class="chart-title">近{{ days }}天订单数</div>
            <div ref="orderChartRef" class="echart" />
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card" v-loading="loadingTrend">
            <div class="chart-title">近{{ days }}天成交额（已完成）</div>
            <div ref="gmvChartRef" class="echart" />
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed, watch, nextTick, onBeforeUnmount } from 'vue'
import request from '../../utils/request'
import * as echarts from 'echarts/core'
import { BarChart, LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([BarChart, LineChart, GridComponent, TooltipComponent, CanvasRenderer])

const loading = ref(false)
const loadingTrend = ref(false)
const days = ref(7)
const stats = reactive({
  userTotal: 0,
  userToday: 0,
  goodsTotal: 0,
  goodsOnSale: 0,
  goodsToday: 0,
  orderTotal: 0,
  orderToday: 0,
  orderCompletedTotal: 0,
  orderCompletedToday: 0,
  gmvTotal: 0,
  gmvToday: 0,
  pointGoodsTotal: 0,
  pointOrderTotal: 0,
  pointOrderToday: 0
})

const trend = ref([])

const orderChartRef = ref(null)
const gmvChartRef = ref(null)
let orderChart = null
let gmvChart = null

const formatMoney = (v) => {
  const n = Number(v || 0)
  return n.toFixed(2)
}

const fetchStats = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/admin/dashboard/stats')
    stats.userTotal = res.userTotal || 0
    stats.userToday = res.userToday || 0
    stats.goodsTotal = res.goodsTotal || 0
    stats.goodsOnSale = res.goodsOnSale || 0
    stats.goodsToday = res.goodsToday || 0
    stats.orderTotal = res.orderTotal || 0
    stats.orderToday = res.orderToday || 0
    stats.orderCompletedTotal = res.orderCompletedTotal || 0
    stats.orderCompletedToday = res.orderCompletedToday || 0
    stats.gmvTotal = res.gmvTotal || 0
    stats.gmvToday = res.gmvToday || 0
    stats.pointGoodsTotal = res.pointGoodsTotal || 0
    stats.pointOrderTotal = res.pointOrderTotal || 0
    stats.pointOrderToday = res.pointOrderToday || 0
  } catch (e) {
  } finally {
    loading.value = false
  }
}

const fetchTrend = async () => {
  loadingTrend.value = true
  try {
    const res = await request.get('/api/admin/dashboard/trend', { params: { days: days.value } })
    trend.value = Array.isArray(res) ? res : []
  } catch (e) {
    trend.value = []
  } finally {
    loadingTrend.value = false
  }
}

const categories = computed(() => trend.value.map(i => String(i.date || '').slice(5)))
const orderSeries = computed(() => trend.value.map(i => Number(i.orderCount || 0)))
const gmvSeries = computed(() => trend.value.map(i => Number(i.gmv || 0)))

const ensureCharts = async () => {
  await nextTick()
  if (orderChartRef.value && !orderChart) {
    orderChart = echarts.init(orderChartRef.value)
  }
  if (gmvChartRef.value && !gmvChart) {
    gmvChart = echarts.init(gmvChartRef.value)
  }
}

const renderCharts = async () => {
  await ensureCharts()
  if (orderChart) {
    orderChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 16, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: categories.value, axisTick: { alignWithLabel: true } },
      yAxis: { type: 'value' },
      series: [{ name: '订单数', type: 'bar', data: orderSeries.value, itemStyle: { color: '#409EFF' }, barMaxWidth: 24 }]
    }, true)
  }
  if (gmvChart) {
    gmvChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 16, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: categories.value, axisTick: { alignWithLabel: true } },
      yAxis: { type: 'value' },
      series: [{ name: '成交额', type: 'line', smooth: true, data: gmvSeries.value, itemStyle: { color: '#67C23A' }, areaStyle: { color: 'rgba(103,194,58,0.15)' } }]
    }, true)
  }
}

const handleResize = () => {
  if (orderChart) orderChart.resize()
  if (gmvChart) gmvChart.resize()
}

onMounted(() => {
  fetchStats()
  fetchTrend()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (orderChart) {
    orderChart.dispose()
    orderChart = null
  }
  if (gmvChart) {
    gmvChart.dispose()
    gmvChart = null
  }
})

watch(() => trend.value, () => {
  renderCharts()
}, { deep: true })

watch(() => loadingTrend.value, (v) => {
  if (!v) renderCharts()
})
</script>

<style scoped>
.admin-dashboard {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.stat-section {
  margin-bottom: 6px;
}
.stat-section :deep(.el-row) {
  row-gap: 12px;
}
.chart-section {
  margin-top: 6px;
}
.chart-section :deep(.el-row) {
  row-gap: 12px;
}
.title {
  font-size: 18px;
  font-weight: 800;
  color: #2c3e50;
}
.stat-card {
  border-radius: 12px;
}
.stat-label {
  color: #909399;
  font-size: 13px;
}
.stat-value {
  margin-top: 10px;
  font-size: 26px;
  font-weight: 800;
  color: #303133;
}
.stat-sub {
  margin-top: 6px;
  color: #909399;
  font-size: 13px;
  display: flex;
  justify-content: flex-start;
  align-items: center;
  gap: 6px;
}
.chart-card {
  border-radius: 12px;
}
.chart-title {
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 10px;
}
.echart {
  width: 100%;
  height: 220px;
}
</style>
