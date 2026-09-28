const { request, getUserId, showError } = require('../../utils/request')

Page({
  data: { today: '', tasks: [] },

  onShow() {
    const d = new Date()
    const today = d.getFullYear() + '-' +
      String(d.getMonth() + 1).padStart(2, '0') + '-' +
      String(d.getDate()).padStart(2, '0')
    this.setData({ today })
    this.loadTasks()
  },

  async loadTasks() {
    try {
      const data = await request({
        path: '/api/tasks',
        query: { userId: getUserId(), date: this.data.today }
      })
      this.setData({ tasks: data.data })
    } catch (error) {
      showError(error, '加载失败：')
    }
  },

  async onComplete(e) {
    try {
      await request({
        path: '/api/tasks/' + e.currentTarget.dataset.id + '/complete',
        method: 'PUT',
        query: { userId: getUserId() }
      })
      this.loadTasks()
    } catch (error) {
      showError(error, '操作失败：')
    }
  },

  onDelete(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '确认删除',
      content: '确定删除这条任务吗？',
      success: async (res) => {
        if (!res.confirm) return
        try {
          await request({ path: '/api/tasks/' + id, method: 'DELETE', query: { userId: getUserId() } })
          this.loadTasks()
        } catch (error) {
          showError(error, '删除失败：')
        }
      }
    })
  },

  onAdd() {
    wx.showModal({
      title: '添加任务',
      editable: true,
      placeholderText: '请输入任务标题，如：检查番茄植株',
      success: async (res) => {
        if (!res.confirm || !res.content || !res.content.trim()) return
        const d = new Date()
        const time = String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0')
        try {
          await request({
            path: '/api/tasks',
            method: 'POST',
            data: {
              userId: getUserId(),
              title: res.content.trim(),
              taskDate: this.data.today,
              taskTime: time,
              status: 0
            }
          })
          this.loadTasks()
        } catch (error) {
          showError(error, '添加失败：')
        }
      }
    })
  }
})
