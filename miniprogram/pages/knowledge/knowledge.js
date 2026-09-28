Page({
  data: {
    list: [
      {
        name: '番茄叶霉病',
        crop: '番茄',
        icon: '🍅',
        symptoms: '叶片出现黄色斑块，叶片背面可能出现霉层。',
        advice: '加强通风，合理控制田间湿度，使用腐霉利、异菌脲等药剂防治。'
      },
      {
        name: '苹果炭疽病',
        crop: '苹果',
        icon: '🍎',
        symptoms: '果实表面出现褐色圆形病斑，逐渐扩大并凹陷。',
        advice: '及时清除病果，喷施咪鲜胺、代森锰锌等药剂。'
      },
      {
        name: '黄瓜白粉病',
        crop: '黄瓜',
        icon: '🥒',
        symptoms: '叶片表面出现白色粉状霉层，严重时叶片枯黄。',
        advice: '加强通风，使用三唑酮、戊唑醇等药剂防治。'
      },
      {
        name: '蚜虫',
        crop: '多种作物',
        icon: '🐛',
        symptoms: '嫩叶和嫩芽上聚集大量绿色或黑色小虫，叶片卷曲。',
        advice: '使用吡虫啉、啶虫脒等药剂喷雾防治，保护天敌瓢虫。'
      },
      {
        name: '红蜘蛛',
        crop: '多种作物',
        icon: '🕷️',
        symptoms: '叶片出现细密黄白色小点，严重时叶片枯黄脱落。',
        advice: '使用阿维菌素、哒螨灵等药剂，注意叶背面喷施。'
      }
    ],
    filteredList: []
  },

  onLoad() {
    this.setData({ filteredList: this.data.list });
  },

  onSearch(e) {
    const keyword = e.detail.value.trim();
    if (!keyword) {
      this.setData({ filteredList: this.data.list });
      return;
    }
    const filtered = this.data.list.filter(item =>
      item.name.includes(keyword) || item.crop.includes(keyword)
    );
    this.setData({ filteredList: filtered });
  },

  showDetail(e) {
    const item = this.data.filteredList[e.currentTarget.dataset.index];
    wx.showModal({
      title: item.name,
      content: `症状：${item.symptoms}\n\n防治：${item.advice}`,
      showCancel: false
    });
  }
});