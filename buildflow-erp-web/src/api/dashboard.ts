import { get } from '@/utils/request'

/**
 * 获取数据大屏聚合数据
 * @returns 大屏所有统计指标数据
 */
export const getDashboardData = () => get('/dashboard/data')
