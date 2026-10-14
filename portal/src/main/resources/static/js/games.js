/*
 * Браузерные тренажёры портала. Данные каждой игры лежат в
 * portal/src/main/resources/games/<id>.yaml и приходят через GET /api/games/<id>.
 * Результат игры отправляется в POST /api/progress/game — сервер хранит лучший счёт
 * и начисляет опыт.
 */
window.Games = (() => {
  const esc = s => App.esc(s);
  const md = s => App.renderMarkdown(String(s ?? ''));
  const shuffle = arr => {
    const a = [...arr];
    for (let i = a.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [a[i], a[j]] = [a[j], a[i]];
    }
    return a;
  };
  const load = name => App.api('/games/' + name);
  // localStorage — только для удобства (пройденные уровни); без него всё тоже работает
  const store = {
    get(key, def) { try { return JSON.parse(localStorage.getItem('qa-games:' + key)) ?? def; } catch (e) { return def; } },
    set(key, val) { try { localStorage.setItem('qa-games:' + key, JSON.stringify(val)); } catch (e) { /* приватный режим */ } }
  };

  async function report(game, score) {
    try {
      App.setProgress(await App.api('/progress/game', { game, score: Math.round(score) }));
    } catch (e) { /* портал недоступен — счёт просто не сохранится */ }
  }

  const GAMES = [
    { id: 'qa-marathon', icon: '🏃', title: 'QA Marathon', play: marathon,
      desc: 'Викторина по всему курсу: теория, Java, API, UI, SQL, Kafka, CI. Режимы «10 вопросов» и «Выживание».' },
    { id: 'priority-poker', icon: '🃏', title: 'Priority Poker', play: poker,
      desc: 'Оцените severity и priority бага по описанию бизнес-последствий.' },
    { id: 'log-detective', icon: '🔎', title: 'Log Detective', play: g => caseQuiz(g, 'levels'),
      desc: 'Лог упавшего автотеста — найдите первопричину: баг продукта, ошибка теста или окружение.' },
    { id: 'pojo-builder', icon: '🧱', title: 'POJO Builder', play: pojo,
      desc: 'По JSON напишите Java-модель. Портал скомпилирует её и проверит Jackson’ом.' },
    { id: 'locator-shock', icon: '⚡', title: 'Locator Shock', play: locators,
      desc: 'Напишите CSS или XPath локатор, который переживёт редизайн страницы.' },
    { id: 'api-circus', icon: '🎪', title: 'API Circus', play: g => caseQuiz(g, 'levels'),
      desc: 'REST, GraphQL, SOAP, gRPC, WebSocket, Kafka: найдите нарушение контракта.' },
    { id: 'system-autopsy', icon: '🩺', title: 'System Autopsy', play: autopsy,
      desc: 'Дежурство: алерт, метрики, логи, трейсы, деплои. Найдите первопричину за минимум шагов.' }
  ];

  function list(el) {
    const best = App.progress.gameBest || {};
    el.innerHTML = `<h1>Тренажёры</h1>
      <p class="muted">Короткие игры для закрепления навыков. Лучший результат сохраняется в прогрессе и приносит опыт.</p>
      <div class="grid">${GAMES.map(g => `
        <a class="card module-card" href="#/game/${g.id}">
          <h3>${g.icon} ${esc(g.title)}</h3>
          <p class="muted">${esc(g.desc)}</p>
          <p>${best[g.id] !== undefined ? `🏆 Рекорд: <b>${best[g.id]}</b>` : '<span class="muted">Ещё не играли</span>'}</p>
        </a>`).join('')}
        <a class="card module-card" href="#/testlab"><h3>🧪 Test Lab</h3>
          <p class="muted">30 багов «из продакшена»: напишите автотест, который падает на баге и проходит после исправления.</p></a>
      </div>`;
  }

  async function open(id, el) {
    const game = GAMES.find(g => g.id === id);
    if (!game) {
      el.innerHTML = '<h1>Тренажёр не найден</h1><p><a href="#/games">Ко всем тренажёрам</a></p>';
      return;
    }
    el.innerHTML = `<div class="breadcrumbs"><a href="#/games">Тренажёры</a> / ${esc(game.title)}</div>
      <h1>${game.icon} ${esc(game.title)}</h1><p class="muted">${esc(game.desc)}</p>
      <div class="game-area"><p class="muted">Загрузка…</p></div>`;
    const area = el.querySelector('.game-area');
    try {
      await game.play({ id, area, data: await load(id) });
    } catch (e) {
      area.innerHTML = `<div class="result-box bad">Не удалось загрузить игру: ${esc(e.message)}</div>`;
    }
  }

  // Общий финальный экран
  function finish(g, score, text, again) {
    report(g.id, score);
    g.area.innerHTML = `<div class="card"><h2>Результат: ${Math.round(score)}</h2><p>${text}</p>
      <button class="btn" id="again">Ещё раз</button> <a class="btn secondary" href="#/games">Ко всем тренажёрам</a></div>`;
    g.area.querySelector('#again').onclick = again;
  }

  // Кнопки вариантов: первый вариант в данных всегда правильный, показываем вперемешку
  function options(container, opts, onAnswer) {
    const order = shuffle(opts.map((text, i) => ({ text, ok: i === 0 })));
    container.innerHTML = `<div class="option-list">${order.map((o, i) =>
      `<button type="button" data-i="${i}">${App.esc(o.text)}</button>`).join('')}</div>`;
    container.querySelectorAll('button').forEach(b => b.onclick = () => {
      const chosen = order[b.dataset.i];
      container.querySelectorAll('button').forEach(x => {
        x.disabled = true;
        if (order[x.dataset.i].ok) x.classList.add('correct');
      });
      if (!chosen.ok) b.classList.add('wrong');
      onAnswer(chosen.ok);
    });
  }

  // ---------- QA Marathon ----------
  function marathon(g) {
    // В YAML вопросы сгруппированы по темам: topics: { Тема: [{q, o: [верный, ...], e}] }
    const all = Object.entries(g.data.topics).flatMap(([topic, qs]) =>
      qs.map(x => ({ topic, q: x.q, options: x.o, explain: x.e })));
    const topics = [...new Set(all.map(q => q.topic))];
    g.area.innerHTML = `<div class="card">
      <p>В банке <b>${all.length}</b> вопросов.</p>
      <p><label>Тема: <select id="topic"><option value="">Все темы</option>
        ${topics.map(t => `<option>${esc(t)}</option>`).join('')}</select></label></p>
      <button class="btn" id="ten">10 вопросов</button>
      <button class="btn secondary" id="surv">Выживание (3 жизни)</button></div>`;
    const pick = () => {
      const t = g.area.querySelector('#topic').value;
      return shuffle(all.filter(q => !t || q.topic === t));
    };
    g.area.querySelector('#ten').onclick = () => run(pick().slice(0, 10), false);
    g.area.querySelector('#surv').onclick = () => run(pick(), true);

    function run(qs, survival) {
      let i = 0, correct = 0, lives = 3;
      const step = () => {
        if (i >= qs.length || (survival && lives <= 0)) {
          const score = survival ? correct * 10 : (correct / qs.length) * 100;
          const game = { ...g, id: survival ? 'qa-marathon-survival' : 'qa-marathon' };
          return finish(game, score, survival
            ? `Правильных ответов подряд до потери всех жизней: <b>${correct}</b>.`
            : `Правильно: <b>${correct}</b> из ${qs.length}.`, () => marathon(g));
        }
        const q = qs[i];
        g.area.innerHTML = `<div class="hud"><span>Вопрос ${i + 1}${survival ? '' : ' / ' + qs.length}</span>
          <span>✅ ${correct}</span>${survival ? `<span>${'❤️'.repeat(lives)}</span>` : ''}
          <span class="muted">${esc(q.topic)}</span></div>
          <div class="card"><div class="q">${md(q.q)}</div><div class="opts"></div>
          <div class="explain"></div><button class="btn" id="next" hidden>Дальше</button></div>`;
        App.highlight(g.area);
        options(g.area.querySelector('.opts'), q.options, ok => {
          if (ok) correct++; else lives--;
          g.area.querySelector('.explain').innerHTML = md((ok ? '✅ ' : '❌ ') + (q.explain || ''));
          const next = g.area.querySelector('#next');
          next.hidden = false;
          next.focus();
          next.onclick = () => { i++; step(); };
        });
      };
      step();
    }
  }

  // ---------- Priority Poker ----------
  function poker(g) {
    const SEV = ['Blocker', 'Critical', 'Major', 'Minor', 'Trivial'];
    const PRI = ['High', 'Medium', 'Low'];
    const rounds = shuffle(g.data.rounds).slice(0, 8);
    let i = 0, points = 0;
    const max = rounds.length * 12;
    const rate = (scale, a, b) => {
      const d = Math.abs(scale.indexOf(a) - scale.indexOf(b));
      return d === 0 ? 6 : d === 1 ? 3 : 0;
    };
    const step = () => {
      if (i >= rounds.length) {
        return finish(g, points / max * 100, `Очки: <b>${points}</b> из ${max}. Точное попадание — 6 очков, соседнее значение — 3.`, () => poker(g));
      }
      const r = rounds[i];
      let sev = null, pri = null;
      g.area.innerHTML = `<div class="hud"><span>Баг ${i + 1} / ${rounds.length}</span><span>Очки: ${points}</span></div>
        <div class="card"><h3>${esc(r.title)}</h3>${md(r.description)}
        <p><b>Severity</b> (насколько сломано):</p><div class="tabs-inline" id="sev">${SEV.map(s => `<button type="button">${s}</button>`).join('')}</div>
        <p><b>Priority</b> (как срочно чинить):</p><div class="tabs-inline" id="pri">${PRI.map(s => `<button type="button">${s}</button>`).join('')}</div>
        <p><button class="btn" id="go" disabled>Вскрываемся!</button></p><div class="explain"></div></div>`;
      const go = g.area.querySelector('#go');
      const bind = (id, set) => g.area.querySelectorAll(`#${id} button`).forEach(b => b.onclick = () => {
        g.area.querySelectorAll(`#${id} button`).forEach(x => x.classList.toggle('active', x === b));
        set(b.textContent);
        go.disabled = !(sev && pri);
      });
      bind('sev', v => sev = v);
      bind('pri', v => pri = v);
      go.onclick = () => {
        const got = rate(SEV, sev, r.severity) + rate(PRI, pri, r.priority);
        points += got;
        g.area.querySelectorAll('.tabs-inline button').forEach(b => b.disabled = true);
        g.area.querySelector('.explain').innerHTML = `<div class="result-box ${got >= 9 ? 'ok' : 'bad'}">
          <p>Эксперты: <b>${r.severity}</b> / <b>${r.priority}</b>. Вы: ${sev} / ${pri}. +${got} очков.</p>${md(r.explain)}</div>`;
        go.textContent = 'Дальше';
        go.onclick = () => { i++; step(); };
      };
    };
    step();
  }

  // ---------- Кейсы с вариантами: Log Detective, API Circus ----------
  // Уровень: { title, tag?, blocks: [{label, text, lang?}], question, options, explain }
  function caseQuiz(g, key) {
    const levels = shuffle(g.data[key]).slice(0, 8);
    let i = 0, correct = 0;
    const step = () => {
      if (i >= levels.length) {
        return finish(g, correct / levels.length * 100, `Верно: <b>${correct}</b> из ${levels.length}.`, () => caseQuiz(g, key));
      }
      const l = levels[i];
      g.area.innerHTML = `<div class="hud"><span>Дело ${i + 1} / ${levels.length}</span><span>✅ ${correct}</span>
        ${l.tag ? `<span class="badge">${esc(l.tag)}</span>` : ''}</div>
        <div class="card"><h3>${esc(l.title)}</h3>
        ${(l.blocks || []).map(b => `<p><b>${esc(b.label)}</b></p>
          <pre><code class="language-${esc(b.lang || 'plaintext')}">${esc(String(b.text).replace(/\n$/, ''))}</code></pre>`).join('')}
        <p><b>${esc(l.question)}</b></p><div class="opts"></div><div class="explain"></div>
        <button class="btn" id="next" hidden>Дальше</button></div>`;
      App.highlight(g.area);
      options(g.area.querySelector('.opts'), l.options, ok => {
        if (ok) correct++;
        g.area.querySelector('.explain').innerHTML = md((ok ? '✅ ' : '❌ ') + l.explain);
        const next = g.area.querySelector('#next');
        next.hidden = false;
        next.onclick = () => { i++; step(); };
      });
    };
    step();
  }

  // ---------- POJO Builder ----------
  function pojo(g) {
    const levels = g.data.levels;
    const done = store.get('pojo-done', []);
    const menu = () => {
      g.area.innerHTML = `<div class="card"><p>Пройдено уровней: <b>${done.length}</b> из ${levels.length}.</p>
        <div class="option-list">${levels.map((l, i) => `<button type="button" data-i="${i}">
          ${done.includes(l.id) ? '✅' : '⬜'} ${i + 1}. ${esc(l.title)}</button>`).join('')}</div></div>`;
      g.area.querySelectorAll('button[data-i]').forEach(b => b.onclick = () => level(+b.dataset.i));
    };
    const level = idx => {
      const l = levels[idx];
      let attempts = 0;
      g.area.innerHTML = `<p><a href="javascript:void 0" id="back">← К списку уровней</a></p>
        <div class="card"><h3>${idx + 1}. ${esc(l.title)}</h3>${md(l.description)}
        <div class="two-col"><div><p><b>JSON</b></p><pre><code class="language-json">${esc(l.json.trim())}</code></pre></div>
        <div><p><b>Ваш класс</b> (импорты java.util, java.math, java.time добавятся сами)</p>
        <textarea class="code" spellcheck="false">${esc(store.get('pojo-code:' + l.id, l.starter))}</textarea></div></div>
        <p><button class="btn" id="check">Проверить</button>
        <button class="btn secondary" id="hint">💡 Подсказка</button>
        <button class="btn secondary" id="sol" hidden>Показать решение</button></p>
        <div class="hint" hidden>${md(l.hint)}</div><div class="result"></div><div class="solution"></div></div>`;
      App.highlight(g.area);
      const ta = g.area.querySelector('textarea');
      ta.addEventListener('keydown', e => {
        if (e.key !== 'Tab') return;
        e.preventDefault();
        ta.setRangeText('    ', ta.selectionStart, ta.selectionEnd, 'end'); // Tab вставляет 4 пробела, а не уводит фокус
      });
      ta.addEventListener('input', () => store.set('pojo-code:' + l.id, ta.value));
      g.area.querySelector('#back').onclick = menu;
      g.area.querySelector('#hint').onclick = () => (g.area.querySelector('.hint').hidden = false);
      g.area.querySelector('#sol').onclick = () => {
        g.area.querySelector('.solution').innerHTML = `<p><b>Один из правильных вариантов:</b></p><pre><code class="language-java">${esc(l.solution.trim())}</code></pre>`;
        App.highlight(g.area);
      };
      const btn = g.area.querySelector('#check');
      btn.onclick = async () => {
        btn.disabled = true;
        btn.textContent = 'Компилирую…';
        const box = g.area.querySelector('.result');
        try {
          const r = await App.api('/games/pojo/check', { code: ta.value, json: l.json, rootType: l.rootType || null });
          attempts++;
          box.innerHTML = `<div class="result-box ${r.ok ? 'ok' : 'bad'}"><p>${r.ok ? '✅' : '❌'} ${esc(r.message)}</p>
            ${r.details.length ? `<ul>${r.details.map(d => `<li><code>${esc(d)}</code></li>`).join('')}</ul>` : ''}</div>`;
          if (r.ok && !done.includes(l.id)) {
            done.push(l.id);
            store.set('pojo-done', done);
          }
          if (r.ok) report(g.id, done.length / levels.length * 100);
          if (!r.ok && attempts >= 2) g.area.querySelector('#sol').hidden = false;
        } catch (e) {
          box.innerHTML = `<div class="result-box bad">Ошибка запроса: ${esc(e.message)}</div>`;
        } finally {
          btn.disabled = false;
          btn.textContent = 'Проверить';
        }
      };
    };
    menu();
  }

  // ---------- Locator Shock ----------
  // Уровень: { title, task, v1, v2, target, targetV2, hint }. target/targetV2 — правильный элемент в каждой версии.
  function locators(g) {
    const levels = g.data.levels;
    let idx = 0, points = 0;
    const isXpath = s => /^(\/|\(|\.\/)/.test(s.trim());
    const find = (doc, sel) => {
      if (isXpath(sel)) {
        const r = doc.evaluate(sel, doc, null, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);
        return Array.from({ length: r.snapshotLength }, (_, i) => r.snapshotItem(i)).filter(n => n.nodeType === 1);
      }
      return [...doc.querySelectorAll(sel)];
    };
    const frameDoc = html => `<!doctype html><html><head><meta charset="utf-8"><style>
      body{font-family:system-ui,sans-serif;font-size:14px;margin:12px;color:#1f2328}
      .qa-hit{outline:3px solid #cf222e!important;outline-offset:2px}
      button{padding:4px 10px} input{padding:4px}</style></head><body>${html}</body></html>`;

    const step = () => {
      if (idx >= levels.length) {
        return finish(g, points / (levels.length * 10) * 100,
          `Очки: <b>${points}</b> из ${levels.length * 10}. Локатор, переживший редизайн, — 10 очков; работающий только на старой вёрстке — 3.`,
          () => { idx = 0; points = 0; step(); });
      }
      const l = levels[idx];
      let tries = 0;
      g.area.innerHTML = `<div class="hud"><span>Уровень ${idx + 1} / ${levels.length}</span><span>Очки: ${points}</span>
        <span class="badge" id="ver">Вёрстка: v1</span></div>
        <div class="card"><h3>${esc(l.title)}</h3>${md(l.task)}
        <iframe class="dom-frame" sandbox="allow-same-origin" title="Страница уровня"></iframe>
        <details><summary>Показать HTML</summary><pre><code class="language-html">${esc(l.v1.trim())}</code></pre></details>
        <p><input class="text" id="sel" placeholder="CSS, например [data-testid='buy'], или XPath, например //button[text()='Купить']" autocomplete="off"></p>
        <p><button class="btn secondary" id="try">Подсветить</button> <button class="btn" id="check">Проверить с редизайном</button>
        <button class="btn secondary" id="hint">💡 Подсказка</button></p>
        <div class="hint" hidden>${md(l.hint)}</div><div class="result"></div></div>`;
      App.highlight(g.area);
      const frame = g.area.querySelector('iframe');
      const sel = g.area.querySelector('#sel');
      const box = g.area.querySelector('.result');
      const show = html => new Promise(res => { frame.onload = res; frame.srcdoc = frameDoc(html); });
      const mark = (doc, nodes) => {
        doc.querySelectorAll('.qa-hit').forEach(n => n.classList.remove('qa-hit'));
        nodes.forEach(n => n.classList.add('qa-hit'));
      };
      const check = (doc, targetSel, s) => {
        const target = doc.querySelector(targetSel);
        let nodes;
        try { nodes = find(doc, s); } catch (e) { return { error: 'Локатор с ошибкой синтаксиса: ' + e.message }; }
        mark(doc, nodes);
        if (nodes.length === 0) return { error: 'Ничего не найдено.' };
        if (nodes.length > 1) return { error: `Найдено элементов: ${nodes.length}. Нужен ровно один.` };
        if (nodes[0] !== target) return { error: 'Найден один элемент, но не тот (он подсвечен красным).' };
        return { ok: true };
      };
      show(l.v1);
      g.area.querySelector('#hint').onclick = () => (g.area.querySelector('.hint').hidden = false);
      g.area.querySelector('#try').onclick = async () => {
        await show(l.v1);
        g.area.querySelector('#ver').textContent = 'Вёрстка: v1';
        const r = check(frame.contentDocument, l.target, sel.value);
        box.innerHTML = `<div class="result-box ${r.ok ? 'ok' : 'bad'}">${r.ok ? '✅ На текущей вёрстке находит нужный элемент. Теперь проверьте с редизайном.' : '❌ ' + esc(r.error)}</div>`;
      };
      sel.addEventListener('keydown', e => { if (e.key === 'Enter') g.area.querySelector('#try').click(); });
      g.area.querySelector('#check').onclick = async () => {
        if (!sel.value.trim()) return;
        await show(l.v1);
        const r1 = check(frame.contentDocument, l.target, sel.value);
        if (!r1.ok) {
          box.innerHTML = `<div class="result-box bad">❌ Уже на старой вёрстке: ${esc(r1.error)}</div>`;
          return;
        }
        tries++; // очки даются только за первую проверку, дошедшую до редизайна
        await show(l.v2);
        g.area.querySelector('#ver').textContent = 'Вёрстка: v2 (после редизайна)';
        const r2 = check(frame.contentDocument, l.targetV2, sel.value);
        const got = r2.ok ? 10 : 3;
        box.innerHTML = `<div class="result-box ${r2.ok ? 'ok' : 'bad'}">
          ${r2.ok ? '✅ Локатор пережил редизайн! +10' : `💥 После редизайна: ${esc(r2.error)} +3 за старую вёрстку.`}
          <details><summary>HTML после редизайна</summary><pre><code class="language-html">${esc(l.v2.trim())}</code></pre></details>
          ${l.answer ? `<p>Пример устойчивого локатора: <code>${esc(l.answer)}</code></p>` : ''}
          ${l.explain ? md(l.explain) : ''}
          <p><button class="btn" id="next">Следующий уровень</button>
          ${r2.ok ? '' : '<button class="btn secondary" id="retry">Попробовать ещё (без очков)</button>'}</p></div>`;
        App.highlight(box);
        let counted = false;
        const take = () => { if (!counted && tries === 1) points += got; counted = true; };
        box.querySelector('#next').onclick = () => { take(); idx++; step(); };
        const retry = box.querySelector('#retry');
        if (retry) retry.onclick = async () => { take(); await show(l.v1); g.area.querySelector('#ver').textContent = 'Вёрстка: v1'; box.innerHTML = ''; };
      };
    };
    step();
  }

  // ---------- System Autopsy ----------
  // Инцидент: { title, alert, tabs: [{name, text}], question, options, explain }
  function autopsy(g) {
    const incidents = g.data.incidents;
    const menu = () => {
      const best = store.get('autopsy-best', {});
      g.area.innerHTML = `<div class="card"><p>Каждый открытый источник данных стоит 5 очков, неверная версия — 25. Начинаете со 100.</p>
        <div class="option-list">${incidents.map((x, i) => `<button type="button" data-i="${i}">
          ${best[x.id] !== undefined ? '🏆 ' + best[x.id] : '🚨'} ${esc(x.title)}</button>`).join('')}</div></div>`;
      g.area.querySelectorAll('button[data-i]').forEach(b => b.onclick = () => play(incidents[+b.dataset.i]));
    };
    const play = inc => {
      let score = 100;
      const opened = new Set();
      g.area.innerHTML = `<p><a href="javascript:void 0" id="back">← К списку инцидентов</a></p>
        <div class="hud"><span id="score">Очки: 100</span></div>
        <div class="result-box bad"><b>🚨 ALERT</b> ${md(inc.alert)}</div>
        <div class="card"><div class="tabs-inline">${inc.tabs.map((t, i) => `<button type="button" data-i="${i}">${esc(t.name)}</button>`).join('')}</div>
        <div class="tab-view muted">Выберите источник данных. Опытный дежурный открывает только нужное.</div></div>
        <div class="card"><p><b>${esc(inc.question)}</b></p><div class="opts"></div><div class="explain"></div></div>`;
      g.area.querySelector('#back').onclick = menu;
      const upd = () => (g.area.querySelector('#score').textContent = 'Очки: ' + Math.max(0, score));
      g.area.querySelectorAll('.tabs-inline button').forEach(b => b.onclick = () => {
        const t = inc.tabs[+b.dataset.i];
        if (!opened.has(t.name)) { opened.add(t.name); score -= 5; upd(); }
        g.area.querySelectorAll('.tabs-inline button').forEach(x => x.classList.toggle('active', x === b));
        g.area.querySelector('.tab-view').innerHTML = `<pre><code class="language-plaintext">${esc(String(t.text).replace(/\n$/, ''))}</code></pre>`;
        g.area.querySelector('.tab-view').classList.remove('muted');
      });
      // Тут можно ошибаться: неверный ответ снимает очки, но игра продолжается до правильного
      const order = shuffle(inc.options.map((text, i) => ({ text, ok: i === 0 })));
      const opts = g.area.querySelector('.opts');
      opts.innerHTML = `<div class="option-list">${order.map((o, i) => `<button type="button" data-i="${i}">${esc(o.text)}</button>`).join('')}</div>`;
      opts.querySelectorAll('button').forEach(b => b.onclick = () => {
        const o = order[+b.dataset.i];
        if (!o.ok) {
          b.classList.add('wrong');
          b.disabled = true;
          score -= 25;
          upd();
          return;
        }
        b.classList.add('correct');
        opts.querySelectorAll('button').forEach(x => x.disabled = true);
        const final = Math.max(0, score);
        const best = store.get('autopsy-best', {});
        best[inc.id] = Math.max(best[inc.id] ?? 0, final);
        store.set('autopsy-best', best);
        report(g.id, final);
        g.area.querySelector('.explain').innerHTML = `<div class="result-box ok"><p>✅ Первопричина найдена. Очки: <b>${final}</b>
          (открыто источников: ${opened.size}).</p>${md(inc.explain)}</div>
          <button class="btn" id="more">Другой инцидент</button>`;
        g.area.querySelector('#more').onclick = menu;
      });
    };
    menu();
  }

  return { list, open };
})();
