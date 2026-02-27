console.log(`content:start`);
let Kaitomm = {
    onEnd: function () {
        browser.runtime.sendMessage({
            action: "Kaitomm",
            topic: "onEnd"
        });
    },
    onPlay: function () {
        browser.runtime.sendMessage({
            action: "Kaitomm",
            topic: "onPlay"
        });
    },
    onPause: function () {
        browser.runtime.sendMessage({
            action: "Kaitomm",
            topic: "onPause"
        });
    },
    onStop: function () {
        browser.runtime.sendMessage({
            action: "Kaitomm",
            topic: "onStop"
        });
    },
    onError: function (message) {
        browser.runtime.sendMessage({
            action: "Kaitomm",
            topic: "onError",
            data: message
        });
    }
}
window.wrappedJSObject.Kaitomm = cloneInto(
    Kaitomm,
    window,
    { cloneFunctions: true });

browser.runtime.onMessage.addListener((data, sender) => {
    console.log("content:eval:" + data);
    if (data.action === 'evalJavascript') {
        let evalCallBack = {
            id: data.id,
            action: "evalJavascript",
        }
        try {
            let result = window.eval(data.data);
            console.log("content:eval:result" + result);
            if (result) {
                evalCallBack.data = result;
            } else {
                evalCallBack.data = "";
            }
        } catch (e) {
            evalCallBack.data = e.toString();
            return Promise.resolve(evalCallBack);
        }
        return Promise.resolve(evalCallBack);
    }
});
