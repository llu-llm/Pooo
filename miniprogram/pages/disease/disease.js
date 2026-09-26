const USER_ID = 'test001';

Page({
  data: {
    imagePath: '',
    loading: false,
    cropIndex: 0,
    crops: ['自动识别', '番茄', '黄瓜', '辣椒', '茄子', '苹果', '柑橘', '葡萄', '小麦', '水稻'],
    candidates: [],
    activeIndex: 0,
    current: null,
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
          candidates: [],
          current: null,
          qualityTip: ''
        });
        this.uploadImage(path);
      }
    });
  },

  onCropChange(e) {
    this.setData({ cropIndex: e.detail.value });
  },

  uploadImage(filePath) {
    this.setData({ loading: true });

    wx.uploadFile({
      url: getApp().globalData.BASE_URL + '/api/disease/detect',
      filePath: filePath,
      name: 'file',
      formData: {
        userId: USER_ID,                                    // ← 新增 userId
        crop: this.data.crops[this.data.cropIndex]
      },
      success: (res) => {
        this.setData({ loading: false });
        try {
          const data = JSON.parse(res.data);
          if (data.code === 0) {
            this.handleResult(data.data);
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

  handleResult(data) {
    let candidates = [];

    if (data.candidates && data.candidates.length > 0) {
      candidates = data.candidates;
    } else if (data.disease) {
      candidates = [{
        crop: data.crop,
        disease: data.disease,
        confidence: data.confidence,
        symptoms: data.symptoms,
        advice: data.advice,
        disclaimer: data.disclaimer
      }];
    }

    if (candidates.length === 0) {
      this.setData({ qualityTip: '未识别出病害，请检查图片是否模糊、过暗或主体过小' });
      return;
    }

    candidates = candidates.map(c => ({
      ...c,
      confidencePercent: Math.round((c.confidence || 0) * 100)
    }));

    this.setData({
      candidates: candidates,
      activeIndex: 0,
      current: candidates[0]
    });
  },

  onCandidateTap(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({
      activeIndex: index,
      current: this.data.candidates[index]
    });
  },

  goHistory() {
    wx.navigateTo({ url: '/pages/records/record-history/record-history?type=disease' });
  }
});