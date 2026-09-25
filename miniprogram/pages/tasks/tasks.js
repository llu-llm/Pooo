const USER_ID = 'test001';

Page({
  data: {
    today: '',
    tasks: []
  },

  onShow() {
    const d = new Date();
    const today = d.getFullYear() + '-' +
      String(d.getMonth() + 1).padStart(2, '0') + '-' +
      String(d.getDate()).padStart(2, '0');
    this.setData({ today });
    this.loadTasks();
  },

  loadTasks() {
    wx.request({
      url: getApp().globalData.BASE_URL + '/api/tasks?userId=' + USER_ID + '&date=' + this.data.today,
      method: 'GET',
      success: (res) => {
        if (res.data.code === 0) {
          this.setData({ tasks: res.data.data });
        }
      }
    });
  },

  onComplete(e) {
    const id = e.currentTarget.dataset.id;
    wx.request({
      url: getApp().globalData.BASE_URL + '/api/tasks/' + id + '/complete',
      method: 'PUT',
      success: () => this.loadTasks()
    });
  },

  onDelete(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认删除',
      content: '确定删除这条任务吗？',
      success: (res) => {
        if (res.confirm) {
          wx.request({
            url: getApp().globalData.BASE_URL + '/api/tasks/' + id,
            method: 'DELETE',
            success: () => this.loadTasks()
          });
        }
      }
    });
  },

  onAdd() {
    wx.showModal({
      title: '添加任务',
      editable: true,
      placeholderText: '请输入任务标题，如：检查番茄植株',
      success: (res) => {
        if (res.confirm && res.content) {
          const d = new Date();
          const time = String(d.getHours()).padStart(2, '0') + ':' +
                       String(d.getMinutes()).padStart(2, '0');
          wx.request({
            url: getApp().globalData.BASE_URL + '/api/tasks',
            method: 'POST',
            header: { 'Content-Type': 'application/json' },
            data: {
              userId: USER_ID,
              title: res.content,
              taskDate: this.data.today,
              taskTime: time,
              status: 0
            },
            success: () => this.loadTasks()
          });
        }
      }
    });
  }
});