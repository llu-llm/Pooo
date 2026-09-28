Page({
  data: {
    seasonName: '',
    recommend: ''
  },

  onShow() {
    const month = new Date().getMonth() + 1;
    let seasonName = '';
    let recommend = '';

    if (month >= 3 && month <= 4) {
      seasonName = '萌芽期';
      recommend = '拍照识病 ｜ 农药兑水';
    } else if (month >= 5 && month <= 6) {
      seasonName = '开花坐果期';
      recommend = '拍照数果 ｜ 拍照识病';
    } else if (month >= 7 && month <= 8) {
      seasonName = '果实膨大期';
      recommend = '拍照数果 ｜ 农药兑水';
    } else if (month >= 9 && month <= 10) {
      seasonName = '成熟采收期';
      recommend = '拍照数果 ｜ 熟果识别';
    } else {
      seasonName = '休眠期';
      recommend = '农事记录 ｜ 修剪管理';
    }

    this.setData({ seasonName, recommend });
  },

  goDisease() {
    wx.navigateTo({ url: '/pages/disease/disease' });
  },

  goPesticide() {
    wx.navigateTo({ url: '/pages/pesticide/pesticide' });
  },

  goFruit() {
    wx.navigateTo({ url: '/pages/fruit/fruit' });
  },

  goTasks() {
    wx.navigateTo({ url: '/pages/records/records' });
  }
});