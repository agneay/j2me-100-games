/* J2ME 100 Games - site behaviour: nav, catalogue filters, phone menu mode, easter egg. */
(function () {
  "use strict";

  var reduceMotion = window.matchMedia && window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  function $(sel, root) { return (root || document).querySelector(sel); }
  function $all(sel, root) { return Array.prototype.slice.call((root || document).querySelectorAll(sel)); }

  function store(key, value) {
    try {
      if (value === undefined) return localStorage.getItem(key);
      localStorage.setItem(key, value);
    } catch (e) { /* storage may be unavailable */ }
    return null;
  }

  /* mobile nav */
  var menuBtn = $(".menu-btn");
  if (menuBtn) {
    menuBtn.addEventListener("click", function () {
      var nav = $(".nav");
      var open = nav.classList.toggle("open");
      menuBtn.setAttribute("aria-expanded", open ? "true" : "false");
    });
  }

  /* catalogue: search, category filter, sort */
  var catalog = $("#catalog-grid");
  if (catalog) {
    var cards = $all(".card", catalog);
    var search = $("#q");
    var sort = $("#sort");
    var count = $("#result-count");
    var empty = $("#catalog-empty");
    var catButtons = $all(".cats button");
    var activeCat = "all";

    var params = new URLSearchParams(location.search);
    if (params.get("q")) search.value = params.get("q");
    if (params.get("cat")) activeCat = params.get("cat");
    if (params.get("sort")) sort.value = params.get("sort");

    function apply(pushState) {
      var q = search.value.trim().toLowerCase();
      var shown = 0;
      cards.forEach(function (c) {
        var okCat = activeCat === "all" || c.getAttribute("data-category") === activeCat;
        var okQ = !q || c.getAttribute("data-search").indexOf(q) !== -1;
        c.hidden = !(okCat && okQ);
        if (!c.hidden) shown++;
      });
      catButtons.forEach(function (b) {
        b.setAttribute("aria-pressed", b.getAttribute("data-cat") === activeCat ? "true" : "false");
      });
      var key = sort.value;
      var sorted = cards.slice().sort(function (a, b) {
        if (key === "name") return a.getAttribute("data-name").localeCompare(b.getAttribute("data-name"));
        if (key === "category") {
          return a.getAttribute("data-category").localeCompare(b.getAttribute("data-category")) ||
            a.getAttribute("data-id").localeCompare(b.getAttribute("data-id"));
        }
        if (key === "size") return Number(a.getAttribute("data-size")) - Number(b.getAttribute("data-size"));
        if (key === "size-desc") return Number(b.getAttribute("data-size")) - Number(a.getAttribute("data-size"));
        return a.getAttribute("data-id").localeCompare(b.getAttribute("data-id"));
      });
      sorted.forEach(function (c) { catalog.appendChild(c); });
      count.textContent = shown === cards.length ? "Showing all " + shown + " games" :
        "Showing " + shown + " of " + cards.length + " games";
      empty.hidden = shown !== 0;
      if (pushState) {
        var p = new URLSearchParams();
        if (q) p.set("q", q);
        if (activeCat !== "all") p.set("cat", activeCat);
        if (key !== "id") p.set("sort", key);
        var s = p.toString();
        history.replaceState(null, "", location.pathname + (s ? "?" + s : "") + "#catalogue");
      }
    }

    search.addEventListener("input", function () { apply(true); });
    sort.addEventListener("change", function () { apply(true); });
    catButtons.forEach(function (b) {
      b.addEventListener("click", function () {
        activeCat = b.getAttribute("data-cat");
        apply(true);
      });
    });
    apply(false);
  }

  /* classic phone menu mode: browse every game on a 3x3 phone grid */
  var pm = $("#phone-menu");
  var pmOpen = $all("[data-open-phone-menu]");
  if (pm && pmOpen.length && window.GAMES) {
    var games = window.GAMES;
    var sel = 0;
    var grid = $(".pm-grid", pm);
    var pageLabel = $(".pm-page", pm);
    var nameLabel = $(".pm-name", pm);
    var lastFocus = null;

    function render() {
      var page = Math.floor(sel / 9);
      grid.innerHTML = "";
      for (var i = page * 9; i < Math.min(games.length, page * 9 + 9); i++) {
        var g = games[i];
        var a = document.createElement("a");
        a.className = "pm-item" + (i === sel ? " sel" : "");
        a.href = g.url;
        a.setAttribute("aria-label", g.id + " " + g.name);
        a.setAttribute("tabindex", "-1");
        var img = document.createElement("img");
        img.src = g.icon;
        img.alt = "";
        a.appendChild(img);
        a.appendChild(document.createTextNode(g.short));
        (function (idx) {
          a.addEventListener("mouseenter", function () { sel = idx; render(); });
        })(i);
        grid.appendChild(a);
      }
      pageLabel.textContent = (page + 1) + "/" + Math.ceil(games.length / 9);
      nameLabel.textContent = games[sel].id + " " + games[sel].name;
    }

    function open() {
      lastFocus = document.activeElement;
      var saved = Number(store("j2me100:pm"));
      if (saved >= 0 && saved < games.length) sel = saved;
      pm.classList.add("open");
      pm.setAttribute("aria-hidden", "false");
      render();
      $(".pm-screen", pm).focus();
    }

    function close() {
      pm.classList.remove("open");
      pm.setAttribute("aria-hidden", "true");
      if (lastFocus) lastFocus.focus();
    }

    function move(d) {
      sel = Math.max(0, Math.min(games.length - 1, sel + d));
      store("j2me100:pm", String(sel));
      render();
    }

    pmOpen.forEach(function (b) { b.addEventListener("click", open); });
    $(".pm-close", pm).addEventListener("click", close);
    pm.addEventListener("click", function (e) { if (e.target === pm) close(); });
    $all("[data-pm]", pm).forEach(function (b) {
      b.addEventListener("click", function () {
        var k = b.getAttribute("data-pm");
        if (k === "up") move(-3);
        else if (k === "down") move(3);
        else if (k === "left") move(-1);
        else if (k === "right") move(1);
        else if (k === "ok") location.href = games[sel].url;
        else if (k === "back") close();
        else if (/^[1-9]$/.test(k)) { sel = Math.min(games.length - 1, Math.floor(sel / 9) * 9 + Number(k) - 1); render(); }
      });
    });
    pm.addEventListener("keydown", function (e) {
      var k = e.key;
      if (k === "ArrowUp" || k === "2") move(-3);
      else if (k === "ArrowDown" || k === "8") move(3);
      else if (k === "ArrowLeft" || k === "4") move(-1);
      else if (k === "ArrowRight" || k === "6") move(1);
      else if (k === "PageDown" || k === "#") move(9);
      else if (k === "PageUp" || k === "*") move(-9);
      else if (k === "Enter" || k === "5") location.href = games[sel].url;
      else if (k === "Escape") close();
      else if (/^[1379]$/.test(k)) { sel = Math.min(games.length - 1, Math.floor(sel / 9) * 9 + Number(k) - 1); render(); }
      else return;
      e.preventDefault();
    });
  }

  /* easter egg: dial *#06# anywhere outside a text field */
  var egg = $("#egg");
  var typed = "";
  document.addEventListener("keydown", function (e) {
    if (!egg || /input|textarea|select/i.test(e.target.tagName) || e.target.isContentEditable) return;
    if (e.key.length !== 1) return;
    typed = (typed + e.key).slice(-5);
    if (typed === "*#06#") {
      var n = window.GAMES ? window.GAMES.length : 100;
      egg.textContent = "IMEI: 100-J2ME-" + String(n).padStart(3, "0") + "-GAMES-0 — serial verified, snake still hungry.";
      egg.classList.add("show");
      setTimeout(function () { egg.classList.remove("show"); }, reduceMotion ? 8000 : 5000);
      typed = "";
    }
  });
})();
