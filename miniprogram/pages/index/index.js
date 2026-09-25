Page({
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
    wx.navigateTo({ url: '/pages/tasks/tasks' });
  }
});