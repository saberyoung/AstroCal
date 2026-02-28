package com.yangsheng.astrocal.ui.i18n

enum class Lang { ZH_HANS, ZH_HANT, EN, IT, JA, KO }

fun strings(lang: Lang): UiStrings = when (lang) {

    Lang.ZH_HANS -> UiStrings(
        appName = "AstroCal · 星算",
        appSubtitle = "快速天文工具集",
        chooseLang = "选择语言",
        enter = "进入",
        exit = "退出",
        exitTitle = "退出",
        exitConfirmText = "确定要退出吗？",
        yes = "确定",
        cancel = "取消",

        homeTitle = "功能",
        finderTitle = "寻星图",
        finderDesc = "泛星计划（PS1）影像 + 盖亚 DR3 星表",

        ToolsTitle = "小工具",
        ToolsDesc = "时间转换 / 角距离 / 更多…",

        timeTitle = "时间转换",
        timeDesc = "ISO → Unix / JD / MJD",
        input = "输入",
        output = "输出",
        unix = "Unix 时间",
        jd = "儒略日",
        mjd = "修正儒略日",

        homeWeatherTitle = "天气预报",
        homeWeatherSubtitle = "预报 + 云量",

        weatherTitle = "天气预报",
        weatherCitySearchTitle = "城市搜索",
        weatherSearchButton = "搜索",
        weatherRefreshButton = "刷新",
        weatherSelectPlace = "请选择地点：",
        weatherNext24h = "未来24小时",

        cloudMapTitle = "云图",
        cloudMapButton = "云图",
        cloudMapOpenInBrowser = "在浏览器打开云图：",
        cloudMapWindy = "Windy：云图",
        cloudMapVentusky = "Ventusky：云图",

        selectLanguageTitle = "选择语言",

        weatherNoResults = "未找到结果",
        weatherCityPlaceLabel = "城市 / 地点",
        weatherSearching = "搜索中...",
        weatherLoading = "加载中...",
        weatherTemp = "气温",
        weatherWind = "风速",
        weatherPrecip = "降水",
        weatherCloud = "云量",
        weatherCloudLMH = "云量 低/中/高",

        angleTitle = "角距离计算",
        angleDesc = "RA/Dec → Angular Separation",
        ra1 = "RA1 (deg)",
        dec1 = "Dec1 (deg)",
        ra2 = "RA2 (deg)",
        dec2 = "Dec2 (deg)",
        sep = "角距离",

        homeCelestialTitle = "天球",
        homeCelestialSubtitle = "Aladin Lite 星空图",

        celestialTitle = "天球",
        celestialControlTitle = "控制",
        celestialSurvey = "底图",
        celestialRaLabel = "赤经（RA）",
        celestialDecLabel = "赤纬（Dec）",
        celestialGoto = "跳转",
        celestialGotoZero = "跳转到 (0, 0)",
        celestialHint = "RA/Dec 可输入度数或时分秒（例如 12:34:56 +12:34:56）。",

        compute = "计算",
        parseError = "解析失败：请检查格式"
    )

    Lang.ZH_HANT -> UiStrings(
        appName = "AstroCal · 星算",
        appSubtitle = "快速天文工具集",
        chooseLang = "選擇語言",
        enter = "進入",
        exit = "退出",
        exitTitle = "退出",
        exitConfirmText = "確定要退出嗎？",
        yes = "確定",
        cancel = "取消",

        homeTitle = "功能",
        finderTitle = "尋星圖",
        finderDesc = "泛星計畫（PS1）影像 + 蓋亞 DR3 星表",

        ToolsTitle = "小工具",
        ToolsDesc = "時間轉換 / 角距離 / 更多…",

        timeTitle = "時間轉換",
        timeDesc = "ISO → Unix / JD / MJD",
        input = "輸入",
        output = "輸出",
        unix = "Unix 時間",
        jd = "儒略日",
        mjd = "修正儒略日",

        homeWeatherTitle = "天氣預報",
        homeWeatherSubtitle = "預報 + 雲量",

        weatherTitle = "天氣預報",
        weatherCitySearchTitle = "城市搜尋",
        weatherSearchButton = "搜尋",
        weatherRefreshButton = "重新整理",
        weatherSelectPlace = "請選擇地點：",
        weatherNext24h = "未來24小時",

        cloudMapTitle = "雲圖",
        cloudMapButton = "雲圖",
        cloudMapOpenInBrowser = "在瀏覽器開啟雲圖：",
        cloudMapWindy = "Windy：雲圖",
        cloudMapVentusky = "Ventusky：雲圖",

        selectLanguageTitle = "選擇語言",

        weatherNoResults = "未找到結果",
        weatherCityPlaceLabel = "城市 / 地點",
        weatherSearching = "搜尋中...",
        weatherLoading = "載入中...",
        weatherTemp = "氣溫",
        weatherWind = "風速",
        weatherPrecip = "降水",
        weatherCloud = "雲量",
        weatherCloudLMH = "雲量 低/中/高",

        angleTitle = "角距離計算",
        angleDesc = "RA/Dec → Angular Separation",
        ra1 = "RA1 (deg)",
        dec1 = "Dec1 (deg)",
        ra2 = "RA2 (deg)",
        dec2 = "Dec2 (deg)",
        sep = "角距離",

        homeCelestialTitle = "天球",
        homeCelestialSubtitle = "Aladin Lite 星空圖",

        celestialTitle = "天球",
        celestialControlTitle = "控制",
        celestialSurvey = "底圖",
        celestialRaLabel = "赤經（RA）",
        celestialDecLabel = "赤緯（Dec）",
        celestialGoto = "跳轉",
        celestialGotoZero = "跳轉到 (0, 0)",
        celestialHint = "RA/Dec 可輸入度數或時分秒（例如 12:34:56 +12:34:56）。",

        compute = "計算",
        parseError = "解析失敗：請檢查格式"
    )

    Lang.EN -> UiStrings(
        appName = "AstroCal",
        appSubtitle = "Quick astronomy utilities",
        chooseLang = "Choose language",
        enter = "Enter",
        exit = "Exit",
        exitTitle = "Exit",
        exitConfirmText = "Are you sure you want to exit?",
        yes = "Yes",
        cancel = "Cancel",

        homeTitle = "Features",
        finderTitle = "Finder Chart",
        finderDesc = "Pan-STARRS (PS1) image + Gaia DR3 catalog",

        ToolsTitle = "Tools",
        ToolsDesc = "Time conversion / Angular separation / more…",

        timeTitle = "Time Conversion",
        timeDesc = "ISO → Unix / JD / MJD",
        input = "Input",
        output = "Output",
        unix = "Unix Time",
        jd = "Julian Date",
        mjd = "Modified Julian Date",

        homeWeatherTitle = "Weather Report",
        homeWeatherSubtitle = "Forecast + cloud cover",

        weatherTitle = "Weather Report",
        weatherCitySearchTitle = "City Search",
        weatherSearchButton = "Search",
        weatherRefreshButton = "Refresh",
        weatherSelectPlace = "Select a place:",
        weatherNext24h = "Next 24 hours",

        cloudMapTitle = "Cloud Map",
        cloudMapButton = "Cloud Map",
        cloudMapOpenInBrowser = "Open cloud map in browser:",
        cloudMapWindy = "Windy: Clouds",
        cloudMapVentusky = "Ventusky: Clouds",

        selectLanguageTitle = "Select Language",

        weatherNoResults = "No results found",
        weatherCityPlaceLabel = "City / Place",
        weatherSearching = "Searching...",
        weatherLoading = "Loading...",
        weatherTemp = "Temperature",
        weatherWind = "Wind",
        weatherPrecip = "Precip",
        weatherCloud = "Cloud",
        weatherCloudLMH = "Cloud L/M/H",

        angleTitle = "Angular Separation",
        angleDesc = "RA/Dec → Angular Separation",
        ra1 = "RA1 (deg)",
        dec1 = "Dec1 (deg)",
        ra2 = "RA2 (deg)",
        dec2 = "Dec2 (deg)",
        sep = "Separation",

        homeCelestialTitle = "Celestial",
        homeCelestialSubtitle = "Aladin Lite sky map",

        celestialTitle = "Celestial",
        celestialControlTitle = "Controls",
        celestialSurvey = "Survey",
        celestialRaLabel = "RA",
        celestialDecLabel = "Dec",
        celestialGoto = "Goto",
        celestialGotoZero = "Goto (0, 0)",
        celestialHint = "RA/Dec accepts degrees or sexagesimal (e.g., 12:34:56 +12:34:56).",

        compute = "Compute",
        parseError = "Parse failed: please check format"
    )

    Lang.IT -> UiStrings(
        appName = "AstroCal",
        appSubtitle = "Strumenti astronomici rapidi",
        chooseLang = "Seleziona lingua",
        enter = "Entra",
        exit = "Esci",
        exitTitle = "Uscita",
        exitConfirmText = "Vuoi davvero uscire?",
        yes = "Sì",
        cancel = "Annulla",

        homeTitle = "Funzioni",
        finderTitle = "Carta di Ricerca",
        finderDesc = "Immagine PS1 + catalogo Gaia DR3",

        ToolsTitle = "Strumenti",
        ToolsDesc = "Conversione tempo / Separazione angolare / altro…",

        timeTitle = "Conversione del tempo",
        timeDesc = "ISO → Unix / JD / MJD",
        input = "Input",
        output = "Output",
        unix = "Tempo Unix",
        jd = "Data Giuliana",
        mjd = "Data Giuliana Modificata",

        homeWeatherTitle = "Meteo",
        homeWeatherSubtitle = "Previsioni + nuvolosità",

        weatherTitle = "Meteo",
        weatherCitySearchTitle = "Ricerca città",
        weatherSearchButton = "Cerca",
        weatherRefreshButton = "Aggiorna",
        weatherSelectPlace = "Seleziona un luogo:",
        weatherNext24h = "Prossime 24 ore",

        cloudMapTitle = "Mappa Nuvole",
        cloudMapButton = "Mappa Nuvole",
        cloudMapOpenInBrowser = "Apri la mappa nuvole nel browser:",
        cloudMapWindy = "Windy: Nuvole",
        cloudMapVentusky = "Ventusky: Nuvole",

        selectLanguageTitle = "Seleziona lingua",

        weatherNoResults = "Nessun risultato",
        weatherCityPlaceLabel = "Città / Luogo",
        weatherSearching = "Ricerca in corso...",
        weatherLoading = "Caricamento...",
        weatherTemp = "Temperatura",
        weatherWind = "Vento",
        weatherPrecip = "Precipitazioni",
        weatherCloud = "Nuvolosità",
        weatherCloudLMH = "Nuvole B/M/A",

        angleTitle = "Separazione angolare",
        angleDesc = "RA/Dec → Separazione",
        ra1 = "RA1 (deg)",
        dec1 = "Dec1 (deg)",
        ra2 = "RA2 (deg)",
        dec2 = "Dec2 (deg)",
        sep = "Separazione",

        homeCelestialTitle = "Cielo",
        homeCelestialSubtitle = "Mappa del cielo (Aladin Lite)",

        celestialTitle = "Cielo",
        celestialControlTitle = "Controlli",
        celestialSurvey = "Mappa",
        celestialRaLabel = "AR (RA)",
        celestialDecLabel = "Dec",
        celestialGoto = "Vai",
        celestialGotoZero = "Vai a (0, 0)",
        celestialHint = "RA/Dec accetta gradi o formato sessagesimale (es. 12:34:56 +12:34:56).",

        compute = "Calcola",
        parseError = "Errore di parsing: controlla il formato"
    )

    Lang.JA -> UiStrings(
        appName = "AstroCal",
        appSubtitle = "高速天文ツール",
        chooseLang = "言語を選択",
        enter = "開始",
        exit = "終了",
        exitTitle = "終了",
        exitConfirmText = "終了しますか？",
        yes = "はい",
        cancel = "キャンセル",

        homeTitle = "機能",
        finderTitle = "ファインダーチャート",
        finderDesc = "PS1 画像 + Gaia DR3 星表",

        ToolsTitle = "ツール",
        ToolsDesc = "時間変換 / 角距離 / その他…",

        timeTitle = "時間変換",
        timeDesc = "ISO → Unix / JD / MJD",
        input = "入力",
        output = "出力",
        unix = "Unix 時間",
        jd = "ユリウス日",
        mjd = "修正ユリウス日",

        homeWeatherTitle = "天気予報",
        homeWeatherSubtitle = "予報 + 雲量",

        weatherTitle = "天気予報",
        weatherCitySearchTitle = "都市検索",
        weatherSearchButton = "検索",
        weatherRefreshButton = "更新",
        weatherSelectPlace = "場所を選択：",
        weatherNext24h = "今後24時間",

        cloudMapTitle = "雲画像",
        cloudMapButton = "雲画像",
        cloudMapOpenInBrowser = "ブラウザで雲画像を開く：",
        cloudMapWindy = "Windy：雲",
        cloudMapVentusky = "Ventusky：雲",

        selectLanguageTitle = "言語を選択",

        weatherNoResults = "結果が見つかりません",
        weatherCityPlaceLabel = "都市 / 場所",
        weatherSearching = "検索中...",
        weatherLoading = "読み込み中...",
        weatherTemp = "気温",
        weatherWind = "風速",
        weatherPrecip = "降水",
        weatherCloud = "雲量",
        weatherCloudLMH = "雲量 低/中/高",

        angleTitle = "角距離計算",
        angleDesc = "RA/Dec → 角距離",
        ra1 = "RA1 (deg)",
        dec1 = "Dec1 (deg)",
        ra2 = "RA2 (deg)",
        dec2 = "Dec2 (deg)",
        sep = "角距離",

        homeCelestialTitle = "天球",
        homeCelestialSubtitle = "Aladin Lite 天球図",

        celestialTitle = "天球",
        celestialControlTitle = "操作",
        celestialSurvey = "マップ",
        celestialRaLabel = "赤経（RA）",
        celestialDecLabel = "赤緯（Dec）",
        celestialGoto = "移動",
        celestialGotoZero = "(0, 0) に移動",
        celestialHint = "RA/Dec は度数または時分秒で入力できます（例：12:34:56 +12:34:56）。",

        compute = "計算",
        parseError = "解析失敗：形式を確認してください"
    )

    Lang.KO -> UiStrings(
        appName = "AstroCal",
        appSubtitle = "빠른 천문 계산 도구",
        chooseLang = "언어 선택",
        enter = "입장",
        exit = "종료",
        exitTitle = "종료",
        exitConfirmText = "종료하시겠습니까?",
        yes = "예",
        cancel = "취소",

        homeTitle = "기능",
        finderTitle = "파인더 차트",
        finderDesc = "PS1 이미지 + Gaia DR3 카탈로그",

        ToolsTitle = "도구",
        ToolsDesc = "시간 변환 / 각거리 / 기타…",

        timeTitle = "시간 변환",
        timeDesc = "ISO → Unix / JD / MJD",
        input = "입력",
        output = "출력",
        unix = "Unix 시간",
        jd = "율리우스 일",
        mjd = "수정 율리우스 일",

        homeWeatherTitle = "날씨 예보",
        homeWeatherSubtitle = "예보 + 구름량",

        weatherTitle = "날씨 예보",
        weatherCitySearchTitle = "도시 검색",
        weatherSearchButton = "검색",
        weatherRefreshButton = "새로고침",
        weatherSelectPlace = "장소 선택:",
        weatherNext24h = "향후 24시간",

        cloudMapTitle = "구름 지도",
        cloudMapButton = "구름 지도",
        cloudMapOpenInBrowser = "브라우저에서 구름 지도를 열기:",
        cloudMapWindy = "Windy: 구름",
        cloudMapVentusky = "Ventusky: 구름",

        selectLanguageTitle = "언어 선택",

        weatherNoResults = "결과 없음",
        weatherCityPlaceLabel = "도시 / 장소",
        weatherSearching = "검색 중...",
        weatherLoading = "로딩 중...",
        weatherTemp = "기온",
        weatherWind = "풍속",
        weatherPrecip = "강수",
        weatherCloud = "구름량",
        weatherCloudLMH = "구름량 저/중/고",

        angleTitle = "각거리 계산",
        angleDesc = "RA/Dec → 각거리",
        ra1 = "RA1 (deg)",
        dec1 = "Dec1 (deg)",
        ra2 = "RA2 (deg)",
        dec2 = "Dec2 (deg)",
        sep = "각거리",
        homeCelestialTitle = "천구",
        homeCelestialSubtitle = "Aladin Lite 하늘지도",

        celestialTitle = "천구",
        celestialControlTitle = "컨트롤",
        celestialSurvey = "지도",
        celestialRaLabel = "적경(RA)",
        celestialDecLabel = "적위(Dec)",
        celestialGoto = "이동",
        celestialGotoZero = "(0, 0)로 이동",
        celestialHint = "RA/Dec는 도(deg) 또는 시:분:초 형식 입력 가능 (예: 12:34:56 +12:34:56).",

        compute = "계산",
        parseError = "파싱 실패: 형식을 확인하세요"
    )
}