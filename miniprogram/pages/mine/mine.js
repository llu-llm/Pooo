const USER_ID = 'test001';

Page({
  data: {
    stats: {
      disease: 0,
      fruit: 0,
      farm: 0
    }
  },

  onShow() {
    this.loadStats();
  },

  loadStats() {
    wx.request({
      url: getApp().globalData.BASE_URL + '/api/activities?userId=' + USER_ID + '&type=all',
      method: 'GET',
      success: (res) => {
        if (res.data.code === 0) {
          const list = res.data.data || [];
          this.setData({
            stats: {
              disease: list.filter(x => x.type === 'disease').length,
              fruit: list.filter(x => x.type === 'fruit').length,
              farm: list.filter(x => x.type === 'farm' || x.type === 'pesticide').length
            }
          });
        }
      },
      fail: () => {}
    });
  },

  showHelp() {
    wx.showModal({
      title: '帮助与反馈',
      content: '如有问题，请联系农技热线：12316',
      showCancel: false
    });
  },

  showAbout() {
    wx.showModal({
      title: '关于我们',
      content: '田间诊 V1.0\nAI农业生产辅助小程序\n\n围绕苹果树全生命周期，提供拍照识病、农药兑水、拍照数果、农事记录四大功能。',
      showCancel: false
    });
  },

  exportData() {
    wx.request({
      url: getApp().globalData.BASE_URL + '/api/activities?userId=' + USER_ID + '&type=all',
      method: 'GET',
      success: (res) => {
        if (res.data.code === 0) {
          const list = res.data.data || [];
          if (list.length === 0) {
            wx.showToast({ title: '暂无数据可导出', icon: 'none' });
            return;
          }

          // 组装文本
          let text = '田间诊-农事记录导出\n\n';
          list.forEach(item => {
            text += `【${item.type}】${item.title}\n`;
            if (item.summary) text += `  ${item.summary}\n`;
            text += `  时间：${item.createdAt}\n\n`;
          });

          // 复制到剪贴板
          wx.setClipboardData({
            data: text,
            success: () => {
              wx.showModal({
                title: '导出成功',
                content: '数据已复制到剪贴板，可粘贴到微信/备忘录保存。',
                showCancel: false
              });
            }
          });
        }
      },
      fail: () => {
        wx.showToast({ title: '导出失败', icon: 'none' });
      }
    });
  }
});