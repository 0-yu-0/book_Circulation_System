import * as echarts from 'echarts'

// Wait until container has size (clientWidth/clientHeight > 0) or becomes visible.
// Uses IntersectionObserver when available, falls back to polling. Returns resolved size.
export function waitForContainerReady(container, { interval = 100, timeout = 10000 } = {}) {
  return new Promise((resolve, reject) => {
    if (!container) return reject(new Error('Container is null'))
    const start = Date.now()

    // quick check
    function sizeReady() {
      const w = container.clientWidth
      const h = container.clientHeight
      return (w > 0 && h > 0) ? { width: w, height: h } : null
    }

    const initial = sizeReady()
    if (initial) return resolve(initial)

    // If IntersectionObserver available, use it to detect when element becomes visible in viewport
    if (typeof IntersectionObserver !== 'undefined') {
      let io = null
      const onIntersect = (entries) => {
        for (const entry of entries) {
          const rect = entry.boundingClientRect || {}
          const w = Math.floor(rect.width || container.clientWidth)
          const h = Math.floor(rect.height || container.clientHeight)
          if (w > 0 && h > 0) {
            try { io.disconnect() } catch (e) {}
            return resolve({ width: w, height: h })
          }
        }
        if (Date.now() - start >= timeout) {
          try { io.disconnect() } catch (e) {}
          return reject(new Error('Container not ready (timeout)'))
        }
      }
      io = new IntersectionObserver(onIntersect, { root: null, threshold: [0, 0.01, 0.1] })
      io.observe(container)

      // Also set a timeout fallback in case IntersectionObserver callbacks don't fire
      setTimeout(() => {
        const s = sizeReady()
        if (s) return resolve(s)
        try { io.disconnect() } catch (e) {}
        return reject(new Error('Container not ready (timeout)'))
      }, timeout)

      return
    }

    // Fallback: polling with exponential backoff up to timeout
    let attempt = 0
    function poll() {
      const s = sizeReady()
      if (s) return resolve(s)
      if (Date.now() - start >= timeout) return reject(new Error('Container not ready (timeout)'))
      attempt++
      const delay = Math.min(timeout - (Date.now() - start), interval * Math.pow(1.5, Math.min(attempt, 6)))
      setTimeout(poll, Math.max(50, Math.floor(delay)))
    }
    poll()
  })
}

// Estimate how many characters fit in the given pixel width.
// avgCharPx: average pixel width per character (approx). Default 7.
export function estimateCharLimit(containerWidth, labelsCount, avgCharPx = 7, padding = 12) {
  if (!labelsCount || labelsCount <= 0) return 12
  const perLabelPx = (containerWidth - padding * 2) / labelsCount
  const charLimit = Math.max(4, Math.floor(perLabelPx / avgCharPx))
  // cap to a reasonable maximum
  return Math.min(charLimit, 40)
}

// Initialize an echarts instance once container is ready. Returns { chart, dispose }
// getOption can be an object or a function({chart, charLimit, size}) => option
export async function initECharts(container, getOption, opts = {}) {
  const { waitOptions = {}, avgCharPx = 7, resize = true } = opts
  const size = await waitForContainerReady(container, waitOptions)
  const chart = echarts.init(container)
  const labelsCount = opts.labelsCount || 0
  const charLimit = estimateCharLimit(size.width, labelsCount || 1, avgCharPx)
  const option = typeof getOption === 'function' ? getOption({ chart, charLimit, size }) : getOption
  chart.setOption(option)

  const handlers = {}
  if (resize) {
    handlers.resize = () => chart.resize()
    window.addEventListener('resize', handlers.resize)
  }

  function dispose() {
    if (handlers.resize) window.removeEventListener('resize', handlers.resize)
    try { chart.dispose() } catch (e) { /* ignore */ }
  }

  return { chart, dispose }
}
