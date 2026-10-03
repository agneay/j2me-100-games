/*
 * Browser host for the J2ME Game Archive demos.
 *
 * The demos are the games' own Java sources compiled to JavaScript with
 * TeaVM, linked against a re-implementation of the MIDP UI subset the games
 * use (see emu/ in the repository). This is NOT the .jar running in a JVM.
 *
 * The compiled game talks to this file through window.j2me:
 *   present(pixels, w, h)  draw a frame (ARGB ints)
 *   keys                   queue of encoded key events, ((code + 1000) << 2) | type
 *   tone(note, ms, vol)    play a beep (WebAudio square wave)
 *   store                  localStorage prefix for record stores
 */
(function () {
  "use strict";

  var KEY_PRESS = 0, KEY_RELEASE = 1, KEY_REPEAT = 2;

  // keyboard -> MIDP key codes (Nokia-style negative codes for the joystick)
  var KEYBOARD = {
    ArrowUp: -1, ArrowDown: -2, ArrowLeft: -3, ArrowRight: -4,
    Enter: -5, " ": -5,
    w: 50, a: 52, s: 56, d: 54, W: 50, A: 52, S: 56, D: 54,
    "0": 48, "1": 49, "2": 50, "3": 51, "4": 52, "5": 53, "6": 54, "7": 55, "8": 56, "9": 57,
    "*": 42, "#": 35, p: 42, P: 42, Escape: -6, q: -6, Q: -6, e: -7, E: -7, Backspace: -7
  };

  var audio = null;

  function tone(note, ms, vol) {
    if (!window.j2me.sound) return;
    try {
      if (!audio) audio = new (window.AudioContext || window.webkitAudioContext)();
      if (audio.state === "suspended") audio.resume();
      var osc = audio.createOscillator();
      var gain = audio.createGain();
      osc.type = "square";
      osc.frequency.value = 440 * Math.pow(2, (note - 69) / 12);
      var t = audio.currentTime;
      gain.gain.setValueAtTime(0.04 * (vol / 100), t);
      gain.gain.exponentialRampToValueAtTime(0.0001, t + ms / 1000);
      osc.connect(gain);
      gain.connect(audio.destination);
      osc.start(t);
      osc.stop(t + ms / 1000 + 0.02);
    } catch (e) {
      /* audio is optional */
    }
  }

  /**
   * Boot a compiled game.
   * opts: { canvas, width, height, slug, src, onExit, onReady }
   */
  function boot(opts) {
    var canvas = opts.canvas;
    canvas.width = opts.width;
    canvas.height = opts.height;
    var ctx = canvas.getContext("2d");
    var image = ctx.createImageData(opts.width, opts.height);
    var data = image.data;
    var queue = [];
    var first = true;

    window.j2me = {
      width: opts.width,
      height: opts.height,
      store: "j2me:" + opts.slug + ":",
      keys: queue,
      sound: opts.sound !== false,
      props: {},
      present: function (px, w, h) {
        var src = px.data || px;
        for (var i = 0, j = 0, n = w * h; i < n; i++, j += 4) {
          var c = src[i];
          data[j] = (c >> 16) & 255;
          data[j + 1] = (c >> 8) & 255;
          data[j + 2] = c & 255;
          data[j + 3] = 255;
        }
        ctx.putImageData(image, 0, 0);
        if (first) {
          first = false;
          if (opts.onReady) opts.onReady();
        }
      },
      tone: tone,
      exit: function () {
        if (opts.onExit) opts.onExit();
      }
    };

    var api = {
      press: function (code) { queue.push(((code + 1000) << 2) | KEY_PRESS); },
      release: function (code) { queue.push(((code + 1000) << 2) | KEY_RELEASE); },
      repeat: function (code) { queue.push(((code + 1000) << 2) | KEY_REPEAT); },
      keyboardCode: function (key) {
        return Object.prototype.hasOwnProperty.call(KEYBOARD, key) ? KEYBOARD[key] : null;
      }
    };

    var script = document.createElement("script");
    script.src = opts.src;
    script.onload = function () {
      if (typeof window.main !== "function") {
        if (opts.onError) opts.onError("demo script did not load");
        return;
      }
      window.main([]);
    };
    script.onerror = function () {
      if (opts.onError) opts.onError("could not download the demo");
    };
    document.body.appendChild(script);
    return api;
  }

  window.J2MERuntime = { boot: boot, tone: tone };
})();
