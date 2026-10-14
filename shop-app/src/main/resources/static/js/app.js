// Небольшой JS магазина. Запросы идут в REST API; токен берётся из cookie автоматически.

// Страница товара: кнопка "В корзину" отправляет POST /api/cart/items
document.addEventListener('DOMContentLoaded', () => {
  const btn = document.getElementById('add-to-cart-js');
  if (btn) {
    btn.addEventListener('click', async () => {
      const qty = parseInt(document.getElementById('qty').value, 10);
      const toast = document.getElementById('toast');
      const show = (text, ok) => { toast.textContent = text; toast.style.background = ok ? '#2b8a3e' : '#c92a2a'; toast.hidden = false; };
      if (btn.dataset.broken === 'true') { show('Добавлено в корзину', true); return; } // BUG-703
      const res = await fetch('/api/cart/items', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ productId: Number(btn.dataset.productId), quantity: qty })
      });
      const body = await res.json();
      if (res.ok) {
        show('Добавлено в корзину', true);
        const counter = document.querySelector('[data-testid=cart-count]');
        if (counter) counter.textContent = body.totalQuantity;
      } else {
        show(body.message || 'Ошибка', false);
      }
    });
  }

  // Отзыв
  const form = document.getElementById('review-form');
  if (form) {
    form.addEventListener('submit', async e => {
      e.preventDefault();
      const data = new FormData(form);
      const res = await fetch(`/api/products/${form.dataset.productId}/reviews`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ text: data.get('text'), rating: Number(data.get('rating')) })
      });
      if (res.ok) location.reload(); else alert('Не удалось отправить отзыв');
    });
  }

  // Страница заказа: статус обновляется сам каждую секунду, пока заказ "в работе"
  const status = document.getElementById('order-status');
  if (status) {
    const active = ['PAID', 'COOKING'];
    const spinner = document.getElementById('status-spinner');
    const poll = async () => {
      if (!active.includes(status.textContent.trim())) { spinner.hidden = true; return; }
      spinner.hidden = false;
      const res = await fetch(`/api/orders/${status.dataset.orderId}`);
      if (res.ok) status.textContent = (await res.json()).status;
      setTimeout(poll, 1000);
    };
    poll();
  }
});
