const { request, showError } = require('../../utils/request')

Page({
  data: { ratio: '', volume: '', result: null },

  onRatioInput(e) { this.setData({ ratio: e.detail.value }) },
  onVolumeInput(e) { this.setData({ volume: e.detail.value }) },

  async onCalc() {
    const ratio = this.data.ratio.replace(/：/g, ':').replace(/\s/g, '')
    const volume = Number(this.data.volume)

    if (!/^\d+(?::\d+)?$/.test(ratio)) {
      wx.showToast({ title: '比例格式不对，例如 1:500', icon: 'none' })
      return
    }
    if (!Number.isFinite(volume) || volume <= 0 || volume > 100) {
      wx.showToast({ title: '药液量应大于 0 且不超过 100L', icon: 'none' })
      return
    }

    wx.showLoading({ title: '计算中...' })
    try {
      const data = await request({
        path: '/api/pesticide/calc',
        method: 'POST',
        data: { ratio, targetVolume: volume, volumeUnit: 'L' }
      })
      this.setData({ result: data.data })
    } catch (error) {
      showError(error, '计算失败：')
    } finally {
      wx.hideLoading()
    }
  }
})
