<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String appName = String.valueOf(request.getAttribute("appName"));
    String appIconUrl = String.valueOf(request.getAttribute("appIconUrl"));
    String provider = String.valueOf(request.getAttribute("provider"));
    String os = String.valueOf(request.getAttribute("os"));
    String storeType = String.valueOf(request.getAttribute("storeType"));
    String appScheme = String.valueOf(request.getAttribute("appScheme"));
    String storeUrl = String.valueOf(request.getAttribute("storeUrl"));
    String crypt = String.valueOf(request.getAttribute("crypt"));
    String accessSeq = String.valueOf(request.getAttribute("access_seq"));
%>
<html>
<head>
    <meta charset="utf-8">
    <title>Deferred Deep Link</title>
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <style>
        body { font-family: Arial, sans-serif; background:#0b1220; margin:0; padding:0; color:#eaeef7; }
        .wrap { min-height:100vh; display:flex; align-items:center; justify-content:center; padding:20px; }
        .card { background:#121a2b; border:1px solid rgba(255,255,255,0.08); border-radius:16px; padding:22px; max-width:520px; width:100%; }
        .row { display:flex; gap:14px; align-items:center; }
        .icon { width:64px; height:64px; border-radius:14px; background:#1b2742; overflow:hidden; }
        .icon img { width:100%; height:100%; object-fit:cover; }
        .meta { margin-top:14px; font-size:13px; opacity:0.85; line-height:1.6; }
        button { margin-top:18px; width:100%; padding:14px; border:0; border-radius:14px; background:#5b8cff; color:#fff; font-size:15px; font-weight:700; cursor:pointer; }
        button:active { transform: scale(0.99); }
    </style>
</head>
<body>
<div class="wrap">
    <div class="card">
        <div class="row">
            <div class="icon"><img src="<%=appIconUrl%>" alt="app"/></div>
            <div>
                <div style="font-size:18px;font-weight:800;"><%=appName%></div>
                <div style="font-size:12px;opacity:0.8;">provider: <%=provider%> | os: <%=os%> | store: <%=storeType%></div>
            </div>
        </div>

        <div class="meta">
            <div>• access_seq: <%=accessSeq%></div>
            <div>• crypt: (hidden)</div>
            <div style="opacity:0.7;">포폴용 샘플 랜딩 UI입니다.</div>
        </div>

        <button onclick="openOrInstall()">다운로드 / 앱 열기</button>
    </div>
</div>

<script>
    const appScheme = "<%=appScheme%>";
    const storeUrl = "<%=storeUrl%>";
    const crypt = "<%=crypt%>";
    const os = "<%=os%>".toUpperCase();

    function openOrInstall() {
        const start = Date.now();
        window.location.href = appScheme;

        setTimeout(() => {
            const elapsed = Date.now() - start;

            // 1.5초 내 앱 전환이 안되면 스토어로 이동
            if (elapsed < 1600) {
                if (os === "ANDROID") {
                    const url = storeUrl + (storeUrl.includes("?") ? "&" : "?") + "crypt=" + encodeURIComponent(crypt);
                    window.location.href = url;
                } else {
                    window.location.href = storeUrl;
                }
            }
        }, 1500);
    }
</script>
</body>
</html>
