Page({
  data: {
    imagePath: '',
    loading: false,
    result: null,
    qualityTip: ''
  },

  onChooseImage() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['camera', 'album'],
      success: (res) => {
        const path = res.tempFiles[0].tempFilePath;
        this.setData({
          imagePath: path,
          result: null,
          qualityTip: ''
        });
        this.uploadImage(path);
      }
    });
  },

  uploadImage(filePath) {
    this.setData({ loading: true });
    wx.uploadFile({
      url: getApp().globalData.BASE_URL + '/api/fruit/count',
      filePath: filePath,
      name: 'file',
      success: (res) => {
        this.setData({ loading: false });
        try {
          const data = JSON.parse(res.data);
          if (data.code === 0) {
            this.setData({ result: data.data });

            // 质量检测提示：数量为 0 时提醒用户图片可能有问题
            if (!data.data.count || data.data.count === 0) {
              this.setData({ qualityTip: '未检测到果实，请检查图片是否模糊、过暗或主体过小' });
            } else {
              this.setData({ qualityTip: '' });
            }
          } else {
            wx.showToast({ title: '识别失败：' + data.message, icon: 'none' });
          }
        } catch (e) {
          wx.showToast({ title: '解析结果失败', icon: 'none' });
        }
      },
      fail: () => {
        this.setData({ loading: false });
        wx.showToast({ title: '上传失败，请检查后端', icon: 'none' });
      }
    });
  },

  goHistory() {
    wx.navigateTo({ url: '/pages/records/record-history/record-history?type=fruit' });
  }
});