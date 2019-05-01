package org.getgraft.service

object RoiService {
  private val dailyFees = 112000.0

  def monthlyRoi(nodesInTier: Long, stakeAmount: Long): Double = {
    BigDecimal(((RoiService.dailyFees / 4 / (nodesInTier * stakeAmount)) * 30) * 100).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
  }
}
