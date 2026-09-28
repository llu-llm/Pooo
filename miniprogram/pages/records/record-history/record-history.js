const USER_ID = 'test001';

Page({
  data: {
    activeType: 'all',
    filterTypes: [
      { label: '全部', value: 'all' },
      { label: '识病', value: 'disease' },
      { label: '数果', value: 'fruit' },
      { label: '农药', value: 'pesticide' },
      { label: '农事', value: 'farm' }
    ],
    groups: []
  },

  onShow() {
    this.loadActivities();
  },

  onFilterChange(e) {
    this.setData({ activeType: e.currentTarget.dataset.type }, () => {
      this.loadActivities();
    });
  },

  loadActivities() {
    wx.request({
      url: getApp().globalData.BASE_URL + '/api/activities?userId=' + USER_ID + '&type=' + this.data.activeType,
      method: 'GET',
      success: (res) => {
        if (res.data.code === 0) {
          const list = res.data.data || [];
          this.setData({ groups: this.groupByDate(list) });
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
      const createdAt = r.createdAt || '';
      const date = createdAt.substring(0, 10) || '未知日期';
      const time = createdAt.substring(11, 16) || '';

      if (!map[date]) map[date] = [];

      map[date].push({
        id: r.id,
        type: r.type,
        title: r.title,
        summary: r.summary || '',
        icon: r.icon || '📌',
        time: time
      });
    });

    const dates = Object.keys(map).sort((a, b) => b.localeCompare(a));

    return dates.map(date => ({
      date,
      items: map[date]
    }));
  }
});