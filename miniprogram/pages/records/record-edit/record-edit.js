const USER_ID = 'test001';
const recorderManager = wx.getRecorderManager();

Page({
  data: {
    mode: 'voice',
    isRecording: false,
    recordTime: 0,
    transcribedText: '',
    title: '',
    content: '',
    category: '其他',
    categories: ['浇水', '施肥', '打药', '修剪', '病虫害', '其他'],
    tempFilePath: ''
  },

  onLoad(options) {
    if (options.mode) {
      this.setData({ mode: options.mode });
    }

    // 录音结束
    recorderManager.onStop((res) => {
      this.setData({ isRecording: false, tempFilePath: res.tempFilePath });
      this.uploadVoice(res.tempFilePath);
    });

    // 录音错误
    recorderManager.onError((err) => {
      this.setData({ isRecording: false });
      wx.showToast({ title: '录音失败', icon: 'none' });
    });
  },

  onRecordStart() {
    this.setData({ isRecording: true, recordTime: 0 });
    this.timer = setInterval(() => {
      this.setData({ recordTime: this.data.recordTime + 1 });
    }, 1000);

    recorderManager.start({
      duration: 60000,
      sampleRate: 16000,
      numberOfChannels: 1,
      encodeBitRate: 256000,
      format: 'pcm',
      frameSize: 50
    });
  },

  onRecordEnd() {
    if (!this.data.isRecording) return;
    clearInterval(this.timer);
    recorderManager.stop();
  },

  onRecordCancel() {
    if (!this.data.isRecording) return;
    clearInterval(this.timer);
    this.setData({ isRecording: false, recordTime: 0 });
    recorderManager.stop();
    wx.showToast({ title: '已取消', icon: 'none' });
  },

  uploadVoice(filePath) {
    wx.showLoading({ title: '识别中...' });
    wx.uploadFile({
      url: getApp().globalData.BASE_URL + '/api/records/voice',
      filePath: filePath,
      name: 'file',
      formData: { userId: USER_ID },
      success: (res) => {
        wx.hideLoading();
        try {
          const data = JSON.parse(res.data);
          if (data.code === 0) {
            const text = data.data.text || '';
            this.setData({
              transcribedText: text,
              title: text.substring(0, 15) || '语音记录'
            });
          } else {
            wx.showToast({ title: '识别失败：' + data.message, icon: 'none' });
          }
        } catch (e) {
          wx.showToast({ title: '解析失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({ title: '上传失败，请检查后端', icon: 'none' });
      }
    });
  },

  onReRecord() {
    this.setData({ transcribedText: '', tempFilePath: '', title: '' });
  },

  onTitleInput(e) { this.setData({ title: e.detail.value }); },
  onContentInput(e) { this.setData({ content: e.detail.value }); },
  onTextInput(e) { this.setData({ transcribedText: e.detail.value }); },
  onCategorySelect(e) { this.setData({ category: e.currentTarget.dataset.category }); },

  onSave() {
    const { title, content, category, transcribedText, mode } = this.data;
    if (!title) {
      wx.showToast({ title: '请填写标题', icon: 'none' });
      return;
    }

    const d = new Date();
    const dateStr = d.getFullYear() + '-' +
      String(d.getMonth() + 1).padStart(2, '0') + '-' +
      String(d.getDate()).padStart(2, '0');
    const timeStr = String(d.getHours()).padStart(2, '0') + ':' +
      String(d.getMinutes()).padStart(2, '0');

    wx.showLoading({ title: '保存中...' });
    wx.request({
      url: getApp().globalData.BASE_URL + '/api/records',
      method: 'POST',
      header: { 'Content-Type': 'application/json' },
      data: {
        userId: USER_ID,
        title: title,
        content: content || transcribedText,
        category: category,
        recordDate: dateStr,
        recordTime: timeStr,
        status: 0,
        inputType: mode === 'voice' ? 'voice' : 'manual'
      },
      success: (res) => {
        wx.hideLoading();
        if (res.data.code === 0) {
          wx.showToast({ title: '保存成功' });
          setTimeout(() => wx.navigateBack(), 800);
        } else {
          wx.showToast({ title: '保存失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({ title: '保存失败，请检查后端', icon: 'none' });
      }
    });
  },

  onUnload() {
    if (this.timer) clearInterval(this.timer);
  }
});