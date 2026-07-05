<template>
  <footer class="app-footer">
    <div class="footer-inner">
      <!-- 品牌简述 -->
      <div class="footer-brand">
        <div class="footer-brand-logo">
          <svg width="36" height="36" viewBox="0 0 24 24" fill="none">
            <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4H6z" fill="url(#footerLogoGrad)" />
            <path d="M3 6h18M16 10a4 4 0 01-8 0" stroke="#fff" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" fill="none" />
            <defs>
              <linearGradient id="footerLogoGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stop-color="#FF4757" />
                <stop offset="100%" stop-color="#FC6621" />
              </linearGradient>
            </defs>
          </svg>
          <span class="brand-name">拼夕夕</span>
        </div>
        <p class="brand-desc">甄选全球好物，品质生活从这里开始</p>
        <div class="footer-social">
          <a href="#" class="social-icon" title="微信">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 01.213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 00.167-.054l1.903-1.114a.864.864 0 01.717-.098 10.16 10.16 0 002.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178A1.17 1.17 0 014.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178 1.17 1.17 0 01-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.902 2.49 3.133 3.948 5.565 3.948.374 0 .737-.035 1.095-.106l1.575.925a.254.254 0 00.13.045c.126 0 .227-.098.227-.23a.362.362 0 00-.037-.167l-.305-1.156a.46.46 0 01.165-.518c1.47-1.073 2.314-2.541 2.314-4.172 0-3.28-2.712-5.88-5.78-6.48a7.43 7.43 0 00-.889-.07zm-2.108 2.926c.498 0 .902.412.902.92a.911.911 0 01-.902.918.911.911 0 01-.902-.918c0-.508.404-.92.902-.92zm4.313 0c.498 0 .902.412.902.92a.911.911 0 01-.902.918.911.911 0 01-.902-.918c0-.508.404-.92.902-.92z"/></svg>
          </a>
          <a href="#" class="social-icon" title="微博">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M20.194 14.197c-.104-2.386-2.554-3.507-4.862-4.066-.722-.174-1.52-.306-1.963-.595-.444-.29-.64-.79-.64-1.285 0-.694.407-1.48 1.017-1.946 1.21-.924 3.813-.84 5.45.09l1.257-2.324C18.533 2.77 15.959 2 13.396 2c-3.37 0-5.553 1.65-5.553 4.37 0 1.293.453 2.487 1.36 3.373.983.96 2.288 1.45 3.44 1.833 1.623.54 3.07.82 3.14 1.733.063.83-.55 1.605-1.353 2.004-.527.26-1.177.423-1.85.497-1.498.164-2.702.08-3.954-.35-.477-.183-.907-.07-1.156.362l-.914 1.505c-.21.362-.09.814.27 1.075 1.673 1.21 4.366 1.667 6.527.573 3.475-1.758 4.99-5.728 5.14-8.432zm-8.88-4.518c-.08-.86.475-1.603 1.254-1.675.79-.07 1.536.51 1.625 1.33.08.82-.475 1.573-1.256 1.654-.78.07-1.545-.508-1.624-1.31zm-2.265 5.742c-.126.763-.756 1.35-1.546 1.41-.79.06-1.53-.53-1.653-1.313-.124-.78.563-1.56 1.354-1.62.79-.06 1.54.51 1.662 1.29.007.08.013.155.02.233h.163zM11.92 3.5c1.855 0 3.49.4 4.773 1.06l-.55 1.02c-.936-.45-2.504-.75-3.33-.23-.493.31-.77.745-.77 1.257 0 .773.64 1.38 1.49 1.75.108.046.217.1.327.16.085.048.17.037.215-.03.16-.243.237-.53.237-.84 0-1.5-1.435-2.5-3.184-2.5-.56 0-1.086.113-1.533.313C9.9 4.593 10.853 3.5 11.92 3.5z"/></svg>
          </a>
          <a href="#" class="social-icon" title="抖音">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><circle cx="12" cy="12" r="11" fill="none" stroke="currentColor" stroke-width="2"/><text x="12" y="16" text-anchor="middle" font-size="9" fill="currentColor" font-weight="bold">抖</text></svg>
          </a>
        </div>
      </div>

      <!-- 链接列 -->
      <div class="footer-links">
        <div class="footer-col">
          <h4>
            <svg class="col-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
            关于拼夕夕
          </h4>
          <a href="#">品牌故事</a>
          <a href="#">隐私政策</a>
          <a href="#">用户协议</a>
          <a href="#">加入我们</a>
        </div>
        <div class="footer-col">
          <h4>
            <svg class="col-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 01-3.46 0"/></svg>
            客户服务
          </h4>
          <a href="#">帮助中心</a>
          <a href="#">售后服务</a>
          <a href="#">退款说明</a>
          <a href="#">意见反馈</a>
        </div>
        <div class="footer-col">
          <h4>
            <svg class="col-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            快捷链接
          </h4>
          <a href="#">购物流程</a>
          <a href="#">会员介绍</a>
          <a href="#">常见问题</a>
          <a href="#">优惠券</a>
        </div>
        <div class="footer-col">
          <h4>
            <svg class="col-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
            配送 & 支付
          </h4>
          <a href="#">快递运输</a>
          <a href="#">配送范围</a>
          <a href="#">在线支付</a>
          <a href="#">货到付款</a>
        </div>
        <div class="footer-col footer-contact">
          <h4>
            <svg class="col-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07 19.5 19.5 0 01-6-6 19.79 19.79 0 01-3.07-8.67A2 2 0 014.11 2h3a2 2 0 012 1.72c.127.96.361 1.903.7 2.81a2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0122 16.92z"/></svg>
            联系我们
          </h4>
          <p class="phone">400-888-8888</p>
          <p>工作时间：9:00 - 21:00</p>
          <p>邮箱：service@youpin.com</p>
          <p>地址：杭州市余杭区拼夕夕大厦</p>
        </div>
      </div>

      <!-- 底部版权 -->
      <div class="footer-bottom">
        <p>© 2024 拼夕夕 版权所有 | ICP备XXXXXXXX号 | 浙公网安备 XXXXXXXXXXXX号</p>
      </div>
    </div>
  </footer>
</template>

<script setup>
</script>

<style scoped>
.app-footer {
  background: #1a1a2e;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 60px;
  position: relative;
  overflow: hidden;
}

.app-footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--primary), var(--accent), var(--primary-light));
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 48px 20px 0;
}

/* ---- 品牌简述区 ---- */
.footer-brand {
  text-align: center;
  padding-bottom: 36px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  margin-bottom: 36px;
}

.footer-brand-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 12px;
}

.brand-name {
  font-size: 22px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--primary), var(--accent));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1px;
}

.brand-desc {
  color: rgba(255, 255, 255, 0.55);
  font-size: 13px;
  margin-bottom: 16px;
}

.footer-social {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.social-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.7);
  transition: all var(--transition);
}

.social-icon:hover {
  background: var(--primary);
  color: #fff;
  transform: translateY(-2px);
}

/* ---- 链接列 ---- */
.footer-links {
  display: flex;
  justify-content: space-between;
  padding-bottom: 36px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  gap: 20px;
}

.footer-col {
  flex: 1;
}

.footer-col h4 {
  font-size: 15px;
  color: #fff;
  margin-bottom: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.col-icon {
  color: var(--primary);
  flex-shrink: 0;
}

.footer-col a {
  display: block;
  color: rgba(255, 255, 255, 0.55);
  font-size: 13px;
  line-height: 2.4;
  text-decoration: none;
  transition: all var(--transition);
  position: relative;
  padding-left: 0;
}

.footer-col a:hover {
  color: var(--primary);
  padding-left: 6px;
}

.footer-contact .phone {
  font-size: 22px;
  color: var(--primary);
  font-weight: 700;
  margin-bottom: 10px;
  letter-spacing: 1px;
}

.footer-contact p {
  color: rgba(255, 255, 255, 0.55);
  font-size: 13px;
  line-height: 2;
  margin: 0;
}

/* ---- 底部版权 ---- */
.footer-bottom {
  text-align: center;
  padding: 20px 0;
  color: rgba(255, 255, 255, 0.35);
  font-size: 12px;
}
</style>