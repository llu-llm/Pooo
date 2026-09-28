function buildUrl(path, query) {
  const baseUrl = getApp().globalData.BASE_URL.replace(/\/$/, '')
  const entries = Object.entries(query || {}).filter(([, value]) => value !== undefined && value !== null && value !== '')
  if (!entries.length) return baseUrl + path
  const search = entries.map(([key, value]) => encodeURIComponent(key) + '=' + encodeURIComponent(value)).join('&')
  return baseUrl + path + (path.includes('?') ? '&' : '?') + search
}

function parseResponse(statusCode, body) {
  if (statusCode < 200 || statusCode >= 300) {
    throw new Error('服务请求失败（HTTP ' + statusCode + '）')
  }
  const data = typeof body === 'string' ? JSON.parse(body) : body
  if (!data || data.code !== 0) {
    throw new Error(data && data.message ? data.message : '服务返回异常')
  }
  return data
}

function request(options) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: buildUrl(options.path, options.query),
      method: options.method || 'GET',
      data: options.data,
      header: options.header || { 'Content-Type': 'application/json' },
      timeout: options.timeout || 30000,
      success: (res) => {
        try {
          resolve(parseResponse(res.statusCode, res.data))
        } catch (error) {
          reject(error)
        }
      },
      fail: (error) => reject(new Error(error.errMsg || '网络连接失败'))
    })
  })
}

function upload(options) {
  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: buildUrl(options.path, options.query),
      filePath: options.filePath,
      name: options.name || 'file',
      formData: options.formData || {},
      timeout: options.timeout || 60000,
      success: (res) => {
        try {
          resolve(parseResponse(res.statusCode, res.data))
        } catch (error) {
          reject(error)
        }
      },
      fail: (error) => reject(new Error(error.errMsg || '文件上传失败'))
    })
  })
}

function getUserId() {
  return wx.getStorageSync('userId') || getApp().globalData.USER_ID
}

function showError(error, prefix) {
  wx.showToast({
    title: (prefix || '') + (error && error.message ? error.message : '请求失败'),
    icon: 'none',
    duration: 2500
  })
}

module.exports = { request, upload, getUserId, showError }
