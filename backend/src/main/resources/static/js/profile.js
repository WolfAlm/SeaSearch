var updateInfoInterval = null;

window.onload = function () {
  setStatistic();
  updateInfoInterval = setInterval(setStatistic, 2000);
}

function createCloud() {
  $.ajax({
    url: window.location.pathname + '/cloudWords',
    method: 'GET',
    cache: false,

    success: function (words) {
      var chart = anychart.tagCloud();
      chart
      .title('Самые популярные слова')
      .angles([0, 10, -20, 25, -15])
      .colorRange(true)
      // set settings for normal state
      .normal({
        fontFamily: 'Times New Roman'
      })
      // set settings for hovered state
      .hovered({
        fill: '#ff3649'
      })
      // set settings for selected state
      .selected({
        fill: '#ff3649',
        fontWeight: 'bold'
      });

      chart.colorRange().labels().fontColor("White");
      var title = chart.title();
      title.fontColor("White");
      title.fontSize(24);
      // Палитра цветов
      var customColor = anychart.scales.ordinalColor();
      customColor.colors(["#9ae3e6", "#a6fbff", "#8df7fc", "#6bf3fa", "#45f6ff", "#00f8ff", "#ffffff"]);
      chart.colorScale(customColor);

      // Подсказка
      chart.tooltip().format("Количество: {%value}\nПопулярность: {%yPercentOfTotal}%");

      var background = chart.background();
      // Заливка фона.
      background.fill("#fff 0.2");

      chart.data(JSON.parse(words));
      chart.container('cloud');
      chart.draw();
    }
  });
}

function createSeries(chart, name) {
  var series1 = chart.series.push(new am4charts.LineSeries());
  series1.dataFields.valueY = "value";
  series1.dataFields.dateX = "date";
  series1.strokeWidth = 1.5;
  series1.smoothing = "monotoneX";
  series1.name = name;
  series1.tooltipText = "{name}: [bold]{valueY}[/]"
  series1.tooltip.pointerOrientation = "vertical";
  series1.stacked = false;
  series1.fillOpacity = 0.6;

  return series1;
}

function createLine(data) {
  const arrayData = JSON.parse(data);

  am4core.ready(function () {
    // Создаем график в контейнере по id #line
    var chart = am4core.create("line", am4charts.XYChart);
    chart.cursor = new am4charts.XYCursor();
    chart.scrollbarX = new am4core.Scrollbar();
    // Add legend
    chart.legend = new am4charts.Legend();
    chart.legend.labels.template.fill = am4core.color("#fff");

    chart.background.fill = '#fff';
    chart.background.fillOpacity = "0.2";

    var valueAxis  = chart.yAxes.push(new am4charts.ValueAxis());
    valueAxis.renderer.labels.template.fill = am4core.color("#FFF");
    // Создаем ось дату.
    var dateAxis = chart.xAxes.push(new am4charts.DateAxis());
    // Цвет оси даты.
    dateAxis.renderer.labels.template.fill = am4core.color("#FFF");
    // Промежутки в датах(само расстояние).
    dateAxis.renderer.minGridDistance = 100;
    // Куда расположить линии графики.
    dateAxis.renderer.grid.template.location = 0;
    dateAxis.startLocation = 0.5;
    dateAxis.endLocation = 0.5;
    // Название оси.
    dateAxis.title.text = "Даты общения";
    dateAxis.title.fill = am4core.color("#FFF");
    dateAxis.title.fontSize = 18;

    dateAxis.dateFormats.setKey("day", "dd MMMM yyyy");
    // Создаем серии.
    var series1 = createSeries(chart, "Все");
    series1.data = arrayData[0];

    var series2 = createSeries(chart, "Входящие");
    series2.data = arrayData[1];

    var series3 = createSeries(chart, "Исходящие");
    series3.data = arrayData[2];

    // Цвет линии.
    series1.stroke = am4core.color("#fff");
    series1.fill = am4core.color("#fff");
    series1.fillOpacity = 0.8;

    series2.stroke = am4core.color("#00ccff");
    series2.fill = am4core.color("#00ccff");

    series3.stroke = am4core.color("#4d6aff");
    series3.fill = am4core.color("#4d6aff");

    // // Цвет линии.
    // series1.stroke = am4core.color("#ff0000");
    // // Цвет подсказки.
    // series1.tooltip.getFillFromObject = false;
    // series1.tooltip.background.fill = am4core.color("#fff");
    // series1.tooltip.label.fill = am4core.color("#000");
  });
}

function restart() {
  $.ajax({
    url: window.location.pathname + '/restart',
    method: 'GET',
    cache: false,
    success: function (data) {
      setStatistic();
      updateInfoInterval = setInterval(setStatistic, 2000);
    }
  });
}

function stopProgress() {
  clearInterval(updateInfoInterval);

  createCloud();

  $.ajax({
    url: window.location.pathname + '/lineWords',
    method: 'GET',
    cache: false,
    success: function (data) {
      createLine(data);
    }
  });
}

function setStatistic() {
  $.ajax({
    url: window.location.pathname + '/progress',
    method: 'GET',
    cache: false,
    success: function (data) {
      if (data.search("done") >= 0) {
        $('.information').html(data);
        stopProgress();
      } else {
        $('.process-parse').html(data);
      }
    }
  });
}