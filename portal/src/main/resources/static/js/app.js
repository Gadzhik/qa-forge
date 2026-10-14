/*
 * Портал курса: маршрутизация по #адресам, отрисовка уроков из Markdown,
 * квизы, задания с кнопкой «Проверить», вкладки для разных ОС.
 */
const App = (() => {
  const $ = (sel, root = document) => root.querySelector(sel);
  const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  const api = async (url, body) => {
    const res = await fetch('/api' + url, body === undefined ? {} : {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body)
    });
    if (!res.ok) throw new Error(res.status + ' ' + res.statusText);
    return res.json();
  };

  let course = [];
  let progress = { lessonsDone: [], quizBest: {}, tasksPassed: {}, gameBest: {}, xp: 0, coins: 0, achievements: [] };
  const content = () => $('#content');

  function setProgress(p) {
    progress = p;
    $('#stats').textContent = `⭐ ${p.xp} XP · 🥒 ${p.coins}`;
    renderSidebar();
  }

  // ---------- Markdown с расширениями ----------
  // ```quiz  — YAML со списком вопросов
  // ```task  — YAML с описанием задания и проверки
  // ```tabs  — YAML "название вкладки: markdown" (например, команды для Windows/macOS/Linux)
  // > 💡 / > ⚠️ / > 🧪 / > ❌ в начале цитаты — цветные блоки-подсказки
  const special = new Set(['quiz', 'task', 'tabs']);
  marked.use({
    gfm: true,
    renderer: {
      code(token) {
        const lang = (token.lang || '').trim().split(/\s+/)[0];
        if (special.has(lang)) {
          return `<div class="x-${lang}" data-src="${esc(token.text)}"></div>`;
        }
        return false; // обычная отрисовка
      }
    }
  });

  function renderMarkdown(md) {
    return marked.parse(md);
  }

  function enhance(root, ctx) {
    root.querySelectorAll('blockquote').forEach(bq => {
      const t = bq.textContent.trim();
      if (t.startsWith('💡')) bq.classList.add('tip');
      else if (t.startsWith('⚠️') || t.startsWith('⚠')) bq.classList.add('warn');
      else if (t.startsWith('🧪')) bq.classList.add('try');
      else if (t.startsWith('❌')) bq.classList.add('bad');
    });
    let quizIndex = 0;
    root.querySelectorAll('.x-tabs').forEach(el => buildTabs(el));
    root.querySelectorAll('.x-quiz').forEach(el => buildQuiz(el, `${ctx}#quiz${++quizIndex}`));
    root.querySelectorAll('.x-task').forEach(el => buildTask(el));
    highlight(root);
    root.querySelectorAll('a[href^="http"]').forEach(a => { a.target = '_blank'; a.rel = 'noopener'; });
  }

  function highlight(root) {
    root.querySelectorAll('pre code').forEach(code => {
      if (code.dataset.done) return;
      code.dataset.done = '1';
      try { hljs.highlightElement(code); } catch (e) { /* неизвестный язык — оставим как есть */ }
      const btn = document.createElement('button');
      btn.className = 'copy-btn';
      btn.textContent = 'Копировать';
      btn.onclick = async () => {
        try { await navigator.clipboard.writeText(code.innerText); btn.textContent = 'Скопировано ✓'; }
        catch { btn.textContent = 'Выделите и Ctrl+C'; }
        setTimeout(() => (btn.textContent = 'Копировать'), 1500);
      };
      code.parentElement.appendChild(btn);
    });
  }

  function parseYaml(el) {
    try { return jsyaml.load(el.dataset.src); }
    catch (e) { el.innerHTML = `<div class="result-box bad">Ошибка в разметке блока: ${esc(e.message)}</div>`; return null; }
  }

  function buildTabs(el) {
    const data = parseYaml(el);
    if (!data) return;
    const names = Object.keys(data);
    const preferred = detectOs();
    let active = names.find(n => n.toLowerCase().includes(preferred)) || names[0];
    el.className = 'tabs';
    el.innerHTML = `<div class="tab-buttons">${names.map(n => `<button type="button" data-tab="${esc(n)}">${esc(n)}</button>`).join('')}</div>`
      + names.map(n => `<div class="tab-panel" data-panel="${esc(n)}">${renderMarkdown(String(data[n]))}</div>`).join('');
    const show = name => {
      el.querySelectorAll('.tab-buttons button').forEach(b => b.classList.toggle('active', b.dataset.tab === name));
      el.querySelectorAll('.tab-panel').forEach(p => (p.hidden = p.dataset.panel !== name));
    };
    el.querySelectorAll('.tab-buttons button').forEach(b => (b.onclick = () => show(b.dataset.tab)));
    show(active);
  }

  function detectOs() {
    const p = (navigator.userAgentData?.platform || navigator.platform || '').toLowerCase();
    if (p.includes('win')) return 'windows';
    if (p.includes('mac')) return 'mac';
    return 'linux';
  }

  function buildQuiz(el, key) {
    const questions = parseYaml(el);
    if (!Array.isArray(questions)) return;
    el.className = 'card quiz';
    const best = progress.quizBest[key];
    el.innerHTML = `<h3>🧠 Проверь себя ${best !== undefined ? `<span class="badge ${best === 100 ? 'ok' : ''}">лучший результат: ${best}%</span>` : ''}</h3>`
      + questions.map((q, qi) => {
        const multi = Array.isArray(q.answer);
        return `<div class="question" data-q="${qi}">
          <div class="q-title">${qi + 1}. ${renderInline(q.q)}${multi ? ' <span class="badge">несколько ответов</span>' : ''}</div>
          ${(q.options || []).map((o, oi) => `<label><input type="${multi ? 'checkbox' : 'radio'}" name="${esc(key)}-${qi}" value="${oi}"> <span>${renderInline(String(o))}</span></label>`).join('')}
          <div class="explain" hidden></div>
        </div>`;
      }).join('')
      + `<button class="btn" type="button">Проверить ответы</button><span class="score"></span>`;
    el.querySelector('button.btn').onclick = async () => {
      let correct = 0;
      questions.forEach((q, qi) => {
        const box = el.querySelector(`[data-q="${qi}"]`);
        const expected = (Array.isArray(q.answer) ? q.answer : [q.answer]).map(Number).sort().join(',');
        const chosen = [...box.querySelectorAll('input:checked')].map(i => Number(i.value)).sort().join(',');
        const ok = expected === chosen;
        if (ok) correct++;
        box.querySelectorAll('label').forEach((l, oi) => {
          l.classList.remove('correct', 'wrong');
          const isAnswer = expected.split(',').includes(String(oi));
          const isChosen = l.querySelector('input').checked;
          if (isAnswer && (ok || isChosen)) l.classList.add('correct');
          else if (isChosen) l.classList.add('wrong');
        });
        const ex = box.querySelector('.explain');
        ex.hidden = false;
        ex.innerHTML = (ok ? '✅ Верно. ' : '❌ Не совсем. ') + (q.explain ? renderInline(q.explain) : '');
      });
      const percent = Math.round(correct * 100 / questions.length);
      el.querySelector('.score').textContent = `Результат: ${correct} из ${questions.length} (${percent}%)`;
      setProgress(await api('/progress/quiz', { key, percent }));
    };
  }

  function renderInline(text) {
    return marked.parseInline(String(text ?? ''));
  }

  function buildTask(el) {
    const t = parseYaml(el);
    if (!t) return;
    const key = 'task:' + t.id;
    const passed = progress.tasksPassed[key];
    el.className = 'card task';
    el.innerHTML = `
      <h3>🛠 Задание: ${esc(t.title)} ${passed ? '<span class="badge ok">выполнено</span>' : ''}</h3>
      <div>${renderMarkdown(t.text || '')}</div>
      ${t.file ? `<p class="path">📄 Файл: <code>${esc(t.file)}</code></p>` : ''}
      <p class="muted path">Проверка в терминале: <code>${esc(mvnw())} -f ${esc(t.module)}/pom.xml test -Dtest=${esc(t.test)}</code></p>
      <button class="btn check" type="button">▶ Проверить</button>
      ${t.hint ? '<button class="btn secondary hint-btn" type="button">💡 Подсказка</button>' : ''}
      ${t.solution ? '<button class="btn secondary solution-btn" type="button">👀 Эталонное решение</button>' : ''}
      <div class="hint" hidden>${renderMarkdown(t.hint || '')}</div>
      <div class="solution" hidden></div>
      <div class="result"></div>`;
    highlight(el);
    const hintBtn = el.querySelector('.hint-btn');
    if (hintBtn) hintBtn.onclick = () => (el.querySelector('.hint').hidden = !el.querySelector('.hint').hidden);
    const solBtn = el.querySelector('.solution-btn');
    if (solBtn) solBtn.onclick = async () => {
      const box = el.querySelector('.solution');
      if (!box.hidden) { box.hidden = true; return; }
      if (!confirm('Сначала попробуйте решить сами — так вы научитесь гораздо больше. Показать решение?')) return;
      const res = await fetch('/api/solution?path=' + encodeURIComponent(t.solution));
      const text = res.ok ? await res.text() : 'Решение не найдено';
      box.innerHTML = `<pre><code class="language-java">${esc(text)}</code></pre>`;
      box.hidden = false;
      highlight(box);
    };
    el.querySelector('.check').onclick = () => runCheck(el, t.module, t.test, key);
  }

  function mvnw() {
    return detectOs() === 'windows' ? 'mvnw.cmd' : './mvnw';
  }

  async function runCheck(el, module, test, key) {
    const btn = el.querySelector('.check');
    const out = el.querySelector('.result');
    btn.disabled = true;
    btn.textContent = '⏳ Проверяю… (Maven запускает тесты, 10–60 секунд)';
    out.innerHTML = '';
    try {
      const r = await api('/check', { module, test, taskKey: key });
      const ok = r.status === 'PASSED';
      let html = `<div class="result-box ${ok ? 'ok' : 'bad'}"><b>${esc(r.summary)}</b>`;
      if (r.tests?.length) {
        html += '<ul>' + r.tests.map(tc => `<li>${tc.passed ? '✅' : '❌'} ${esc(tc.name)}${tc.message ? `<pre>${esc(tc.message)}</pre>` : ''}</li>`).join('') + '</ul>';
      }
      if (r.errors?.length) html += `<pre>${esc(r.errors.join('\n'))}</pre>`;
      html += `<div class="muted" style="font-size:.8rem">${esc(r.command)} · ${(r.millis / 1000).toFixed(1)} с</div></div>`;
      out.innerHTML = html;
      if (ok) {
        setProgress(await api('/progress'));
        const h = el.querySelector('h3');
        if (h && !h.querySelector('.badge.ok')) h.insertAdjacentHTML('beforeend', ' <span class="badge ok">выполнено</span>');
      }
    } catch (e) {
      out.innerHTML = `<div class="result-box bad">Не удалось выполнить проверку: ${esc(e.message)}</div>`;
    } finally {
      btn.disabled = false;
      btn.textContent = '▶ Проверить';
    }
  }

  // ---------- Оглавление ----------
  function renderSidebar() {
    const done = new Set(progress.lessonsDone);
    const [, kind, m, l] = location.hash.split('/');
    $('#sidebar').innerHTML = course.map(mod => {
      const open = (kind === 'l' || kind === 'm') && m === mod.id;
      const doneCount = mod.lessons.filter(x => done.has(mod.id + '/' + x.id)).length;
      return `<details ${open ? 'open' : ''}><summary>${esc(mod.icon)} ${esc(mod.title)} <span class="muted" style="font-weight:400">${doneCount}/${mod.lessons.length}</span></summary><ul>`
        + mod.lessons.map(les => {
          const id = mod.id + '/' + les.id;
          const active = kind === 'l' && m === mod.id && l === les.id;
          return `<li><a href="#/l/${mod.id}/${les.id}" class="${active ? 'active' : ''}"><span class="check">${done.has(id) ? '✓' : ''}</span>${esc(les.title)}</a></li>`;
        }).join('') + '</ul></details>';
    }).join('') + `<details><summary>🎮 Тренажёры</summary><ul>
        <li><a href="#/games">Все тренажёры</a></li><li><a href="#/testlab">Test Lab — охота на баги</a></li>
        <li><a href="#/env">Проверка окружения</a></li><li><a href="#/progress">Мой прогресс</a></li></ul></details>`;
  }

  // ---------- Страницы ----------
  function home() {
    const done = new Set(progress.lessonsDone);
    const total = course.reduce((s, m) => s + m.lessons.length, 0);
    const percent = total ? Math.round(done.size * 100 / total) : 0;
    const next = findNextLesson(done);
    content().innerHTML = `
      <h1>Java QA Automation — с нуля до первой работы</h1>
      <p>Курс для тех, кто никогда не программировал. Вы пройдёте путь от «что такое тестирование» до собственного
      фреймворка автотестов (API, UI, базы данных, Kafka, CI) на учебном приложении <b>QA Burger</b>.</p>
      <div class="card">
        <b>Ваш прогресс: ${done.size} из ${total} уроков (${percent}%)</b>
        <div class="progress-bar"><div style="width:${percent}%"></div></div>
        <p>${next ? `Следующий урок: <a href="#/l/${next.m}/${next.l.id}">${esc(next.l.title)}</a>` : 'Курс пройден! 🎉'}</p>
        <p class="muted">⭐ ${progress.xp} XP · 🥒 ${progress.coins} огурцов · Достижения: ${progress.achievements.length ? progress.achievements.map(esc).join(', ') : 'пока нет'}</p>
      </div>
      <h2>Программа</h2>
      <div class="grid">${course.map(m => {
        const d = m.lessons.filter(x => done.has(m.id + '/' + x.id)).length;
        const p = m.lessons.length ? Math.round(d * 100 / m.lessons.length) : 0;
        return `<a class="card module-card" href="#/m/${m.id}"><h4>${esc(m.icon)} ${esc(m.title)}</h4>
          <div class="muted" style="font-size:.9rem">${esc(m.summary)}</div>
          <div class="progress-bar"><div style="width:${p}%"></div></div>
          <div class="muted" style="font-size:.8rem">${d} / ${m.lessons.length} уроков</div></a>`;
      }).join('')}</div>`;
  }

  function findNextLesson(done) {
    for (const m of course) for (const l of m.lessons) if (!done.has(m.id + '/' + l.id)) return { m: m.id, l };
    return null;
  }

  function modulePage(id) {
    const m = course.find(x => x.id === id);
    if (!m) return notFound();
    const done = new Set(progress.lessonsDone);
    content().innerHTML = `<div class="breadcrumbs"><a href="#/">Курс</a></div>
      <h1>${esc(m.icon)} ${esc(m.title)}</h1><p>${esc(m.summary)}</p>
      <ol>${m.lessons.map(l => `<li><a href="#/l/${m.id}/${l.id}">${esc(l.title)}</a> <span class="muted">· ${l.minutes} мин</span> ${done.has(m.id + '/' + l.id) ? '✅' : ''}</li>`).join('')}</ol>`;
  }

  async function lessonPage(moduleId, lessonId) {
    let lesson;
    try { lesson = await api(`/lesson/${moduleId}/${lessonId}`); } catch { return notFound(); }
    const m = course.find(x => x.id === moduleId);
    const key = moduleId + '/' + lessonId;
    const isDone = progress.lessonsDone.includes(key);
    content().innerHTML = `
      <div class="breadcrumbs"><a href="#/">Курс</a> › <a href="#/m/${moduleId}">${esc(m?.title)}</a></div>
      <h1>${esc(lesson.title)}</h1>
      <div class="lesson-meta">⏱ ~${lesson.minutes} мин чтения и практики</div>
      <article id="lesson-body">${renderMarkdown(lesson.markdown)}</article>
      <div class="lesson-footer">
        <div>${lesson.prev ? `<a class="btn secondary" href="#/l/${lesson.prevModule}/${lesson.prev.id}">← ${esc(lesson.prev.title)}</a>` : ''}</div>
        <button class="btn ${isDone ? 'ok' : ''}" id="done-btn">${isDone ? '✓ Урок пройден' : 'Отметить урок пройденным'}</button>
        <div>${lesson.next ? `<a class="btn secondary" href="#/l/${lesson.nextModule}/${lesson.next.id}">${esc(lesson.next.title)} →</a>` : ''}</div>
      </div>`;
    enhance($('#lesson-body'), key);
    $('#done-btn').onclick = async () => {
      const nowDone = !progress.lessonsDone.includes(key);
      setProgress(await api('/progress/lesson', { key, done: nowDone }));
      if (nowDone && lesson.next) location.hash = `#/l/${lesson.nextModule}/${lesson.next.id}`;
      else lessonPage(moduleId, lessonId);
    };
    window.scrollTo(0, 0);
  }

  async function envPage() {
    content().innerHTML = '<h1>Проверка окружения</h1><p class="muted">Проверяю…</p>';
    const items = await api('/env');
    content().innerHTML = `<h1>Проверка окружения</h1>
      <p>Обязательные пункты нужны с первого урока. Необязательные понадобятся позже — модуль указан в подсказке.</p>
      ${items.map(i => `<div class="env-row"><div class="icon">${i.ok ? '✅' : (i.required ? '❌' : '⚪')}</div>
        <div><b>${esc(i.name)}</b> ${i.required ? '' : '<span class="badge">понадобится позже</span>'}<br>
        <span class="muted">${esc(i.details)}</span>${!i.ok && i.howToFix ? `<br>👉 ${esc(i.howToFix)}` : ''}</div></div>`).join('')}
      <p><button class="btn secondary" onclick="App.route()">Проверить ещё раз</button></p>`;
  }

  async function testlabPage() {
    const data = await api('/testlab');
    const list = data.scenarios || [];
    const passed = list.filter(s => progress.tasksPassed['testlab:' + s.code]).length;
    const stars = n => '★'.repeat(n) + '☆'.repeat(3 - n);
    content().innerHTML = `<h1>🐞 Test Lab — охота на баги</h1>
      <p>В QA Burger заложено ${list.length} багов. У каждого есть «жалоба пользователя». Напишите автотест, который
      <b>проходит на исправном приложении и падает, когда баг включён</b> — именно так в реальной работе пишут регрессионный тест на найденный дефект.</p>
      <blockquote class="tip"><p>💡 Как работать: откройте файл теста в IntelliJ IDEA, напишите проверку, нажмите «Проверить».
      Проверка сама запустит два экземпляра QA Burger — без бага и с багом — и прогонит ваш тест на обоих.
      Чтобы отлаживаться вручную, включите баг на странице <a href="http://localhost:8081/admin" target="_blank">QA Burger → Тренажёр</a>.</p></blockquote>
      <p><b>Пройдено: ${passed} / ${list.length}</b></p>
      ${list.map(s => {
        const ok = progress.tasksPassed['testlab:' + s.code];
        const file = `testlab/src/test/java/ru/qacourse/testlab/student/${s.test}.java`;
        return `<div class="card task" data-code="${esc(s.code)}">
          <h3>${esc(s.code)} · ${esc(s.title)} ${ok ? '<span class="badge ok">пойман</span>' : ''}</h3>
          <p class="muted">${esc(s.area)} · ${s.type === 'ui' ? 'UI-тест (Selenide)' : 'API-тест (REST Assured)'} · сложность ${stars(s.level)}</p>
          <blockquote><p>🗣 «${esc(s.report)}»</p></blockquote>
          <p>Где смотреть: <code>${esc(s.where)}</code></p>
          <p class="path">📄 Ваш тест: <code>${esc(file)}</code></p>
          <button class="btn check" type="button">▶ Проверить</button>
          <button class="btn secondary hint-btn" type="button">💡 Подсказка</button>
          <div class="hint" hidden><p>${esc(s.hint)}</p></div>
          <div class="result"></div></div>`;
      }).join('')}`;
    content().querySelectorAll('.card.task').forEach(card => {
      const s = list.find(x => x.code === card.dataset.code);
      card.querySelector('.hint-btn').onclick = () => (card.querySelector('.hint').hidden = !card.querySelector('.hint').hidden);
      card.querySelector('.check').onclick = () => runCheck(card, 'testlab', s.check, 'testlab:' + s.code);
    });
  }

  function progressPage() {
    content().innerHTML = `<h1>Мой прогресс</h1>
      <div class="card"><p>⭐ Опыт: <b>${progress.xp}</b> · 🥒 Огурцы: <b>${progress.coins}</b></p>
      <p>Уроков пройдено: <b>${progress.lessonsDone.length}</b> · Заданий выполнено: <b>${Object.keys(progress.tasksPassed).length}</b></p>
      <p>Достижения: ${progress.achievements.length ? progress.achievements.map(a => `<span class="badge ok">🏆 ${esc(a)}</span>`).join(' ') : 'пока нет'}</p></div>
      <h2>Рекорды в тренажёрах</h2>
      <ul>${Object.entries(progress.gameBest).map(([g, s]) => `<li>${esc(g)}: ${s}</li>`).join('') || '<li class="muted">Пока нет</li>'}</ul>
      <p class="muted">Прогресс хранится в файле <code>~/.qa-course/progress.json</code>.</p>
      <button class="btn secondary" id="reset">Сбросить прогресс</button>`;
    $('#reset').onclick = async () => {
      if (confirm('Точно сбросить весь прогресс?')) { setProgress(await api('/progress/reset', {})); progressPage(); }
    };
  }

  function notFound() {
    content().innerHTML = '<h1>Страница не найдена</h1><p><a href="#/">На главную</a></p>';
  }

  async function route() {
    document.body.classList.remove('sidebar-open');
    const parts = location.hash.replace(/^#\/?/, '').split('/').filter(Boolean);
    renderSidebar();
    try {
      switch (parts[0]) {
        case undefined: return home();
        case 'm': return modulePage(parts[1]);
        case 'l': return await lessonPage(parts[1], parts[2]);
        case 'env': return await envPage();
        case 'testlab': return await testlabPage();
        case 'progress': return progressPage();
        case 'games': return window.Games ? Games.list(content()) : notFound();
        case 'game': return window.Games ? Games.open(parts[1], content()) : notFound();
        default: return notFound();
      }
    } catch (e) {
      content().innerHTML = `<div class="result-box bad">Ошибка: ${esc(e.message)}</div>`;
    }
  }

  async function init() {
    $('#sidebar-toggle').onclick = () => {
      if (window.innerWidth <= 860) document.body.classList.toggle('sidebar-open');
      else document.body.classList.toggle('sidebar-hidden');
    };
    [course, progress] = await Promise.all([api('/course'), api('/progress')]);
    setProgress(progress);
    window.addEventListener('hashchange', route);
    route();
  }

  document.addEventListener('DOMContentLoaded', init);
  return { route, api, esc, setProgress, renderMarkdown, highlight, get progress() { return progress; } };
})();
