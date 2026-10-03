/* Phone simulator for the Browser Demo on each game page. */
(function () {
  "use strict";

  var root = document.getElementById("sim");
  if (!root || !window.J2MERuntime) return;

  var SIZES = ["128x128", "128x160", "176x208", "176x220", "240x320"];
  var params = new URLSearchParams(location.search);
  var size = params.get("size");
  if (SIZES.indexOf(size) < 0) {
    try { size = localStorage.getItem("j2me100:size"); } catch (e) { size = null; }
    if (SIZES.indexOf(size) < 0) size = "176x208";
  }
  var dims = size.split("x").map(Number);
  var w = dims[0], h = dims[1];
  var scale = Math.min(244 / w, 2);
  var phone = root.querySelector(".phone");
  phone.style.setProperty("--w", Math.round(w * scale) + "px");
  phone.style.setProperty("--h", Math.round(h * scale) + "px");

  var canvas = root.querySelector("canvas");
  var overlay = root.querySelector(".sim-overlay");
  var startBtn = root.querySelector("[data-start]");
  var status = root.querySelector(".sim-status");
  var select = root.querySelector("select[name=size]");
  var soundBtn = root.querySelector("[data-sound]");
  var sound = true;
  try { sound = localStorage.getItem("j2me100:sound") !== "off"; } catch (e) { /* ignore */ }
  var api = null;
  var held = {};

  select.value = size;
  select.addEventListener("change", function () {
    try { localStorage.setItem("j2me100:size", select.value); } catch (e) { /* ignore */ }
    var p = new URLSearchParams(location.search);
    p.set("size", select.value);
    p.set("play", "1");
    location.search = p.toString();
  });

  function setSoundLabel() {
    soundBtn.textContent = sound ? "Sound: on" : "Sound: off";
    soundBtn.setAttribute("aria-pressed", sound ? "true" : "false");
  }
  setSoundLabel();
  soundBtn.addEventListener("click", function () {
    sound = !sound;
    if (window.j2me) window.j2me.sound = sound;
    try { localStorage.setItem("j2me100:sound", sound ? "on" : "off"); } catch (e) { /* ignore */ }
    setSoundLabel();
  });

  function say(text) { if (status) status.textContent = text; }

  function start() {
    if (api) return;
    startBtn.disabled = true;
    say("Loading demo...");
    api = window.J2MERuntime.boot({
      canvas: canvas, width: w, height: h, slug: root.getAttribute("data-slug"),
      src: root.getAttribute("data-src"), sound: sound,
      onReady: function () {
        overlay.hidden = true;
        say("Demo running at " + size + ". Keyboard: arrows, Enter = 5, digits, * and #.");
        canvas.focus();
      },
      onError: function (msg) {
        overlay.hidden = false;
        startBtn.disabled = false;
        api = null;
        overlay.querySelector("p").textContent = "Sorry, " + msg + ". The JAR download still works.";
        say(msg);
      },
      onExit: function () {
        overlay.hidden = false;
        overlay.querySelector("p").textContent = "The game exited. Reload the page to play again.";
        say("Game exited.");
      }
    });
  }
  startBtn.addEventListener("click", start);
  if (params.get("play") === "1") start();

  function press(code) {
    if (!api) return;
    if (held[code]) { api.repeat(code); return; }
    held[code] = true;
    api.press(code);
  }
  function release(code) {
    if (!api || !held[code]) return;
    held[code] = false;
    api.release(code);
  }

  root.querySelectorAll("[data-key]").forEach(function (b) {
    var code = Number(b.getAttribute("data-key"));
    function down(e) {
      e.preventDefault();
      if (!api) start();
      b.classList.add("down");
      press(code);
      if (b.setPointerCapture && e.pointerId !== undefined) b.setPointerCapture(e.pointerId);
    }
    function up() {
      b.classList.remove("down");
      release(code);
    }
    b.addEventListener("pointerdown", down);
    b.addEventListener("pointerup", up);
    b.addEventListener("pointercancel", up);
    b.addEventListener("lostpointercapture", up);
    b.addEventListener("keydown", function (e) {
      if (e.key === "Enter" || e.key === " ") { e.preventDefault(); if (!e.repeat) { b.classList.add("down"); press(code); } }
    });
    b.addEventListener("keyup", function (e) {
      if (e.key === "Enter" || e.key === " ") { e.preventDefault(); up(); }
    });
  });

  function typing(e) {
    var t = e.target;
    return /input|textarea|select/i.test(t.tagName) || t.isContentEditable || (t.tagName === "BUTTON" && !t.hasAttribute("data-key") && t !== canvas);
  }

  document.addEventListener("keydown", function (e) {
    if (!api || typing(e) || e.ctrlKey || e.metaKey || e.altKey) return;
    var code = api.keyboardCode(e.key);
    if (code === null) return;
    e.preventDefault();
    if (e.repeat) api.repeat(code);
    else press(code);
  });
  document.addEventListener("keyup", function (e) {
    if (!api) return;
    var code = api.keyboardCode(e.key);
    if (code === null) return;
    release(code);
  });
  window.addEventListener("blur", function () {
    Object.keys(held).forEach(function (k) { if (held[k]) release(Number(k)); });
  });
})();
