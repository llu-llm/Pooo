const { upload, getUserId, showError } = require('../../utils/request')

Page({
  data: {
    imagePath: '',
    loading: false,
    result: null
  },

  onChooseImage() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['camera', 'album'],
      success: (res) => {
        const path = res.tempFiles[0].tempFilePath
        this.setData({ imagePath: path, result: null })
        this.uploadImage(path)
      }
    })
  },

  async uploadImage(filePath) {
    this.setData({ loading: true })
    try {
      const data = await upload({
        path: '/api/disease/detect',
        filePath,
        formData: { userId: getUserId() }
      })
      this.setData({ result: data.data })
    } catch (error) {
      showError(error, '识别失败：')
    } finally {
      this.setData({ loading: false })
    }
  }
})
