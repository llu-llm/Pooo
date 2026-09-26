const USER_ID = 'test001';

Page({
  data: {
    groups: []
  },

  onShow() {
    this.loadHistory();
  },

  loadHistory() {
    wx.request({
      url: getApp().globalData.BASE_URL + '/api/records/history?userId=' + USER_ID,
      method: 'GET',
      success: (res) => {
        if (res.data.code === 0) {
          this.setData({ groups: this.groupByDate(res.data.data) });
        }
      },
      fail: () => {
        this.setData({ groups: [] });
      }
    });
  },

  groupByDate(records) {
    const map = {};
    records.forEach(r => {
      const date = r.recordDate || '未知日期';
      if (!map[date]) map[date] = [];
      map[date].push(r);
    });
    const dates = Object.keys(map).sort((a, b) => b.localeCompare(a));
    return dates.map(date => ({ date, items: map[date] }));
  }
});