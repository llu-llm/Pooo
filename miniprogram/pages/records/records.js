const USER_ID = 'test001';

Page({
  data: {
    today: '',
    records: []
  },

  onShow() {
    const d = new Date();
    const today = d.getFullYear() + '-' +
      String(d.getMonth() + 1).padStart(2, '0') + '-' +
      String(d.getDate()).padStart(2, '0');
    this.setData({ today });
    this.loadRecords();
  },

  loadRecords() {
    wx.request({
      url: getApp().globalData.BASE_URL + '/api/records?userId=' + USER_ID + '&date=' + this.data.today,
      method: 'GET',
      success: (res) => {
        if (res.data.code === 0) {
          this.setData({ records: res.data.data });
        }
      },
      fail: () => {
        this.setData({ records: [] });
      }
    });
  },

  goVoiceRecord() {
    wx.navigateTo({ url: '/pages/records/record-edit/record-edit?mode=voice' });
  },

  goManualRecord() {
    wx.navigateTo({ url: '/pages/records/record-edit/record-edit?mode=manual' });
  },

  goHistory() {
    wx.navigateTo({ url: '/pages/records/record-history/record-history' });
  }
});