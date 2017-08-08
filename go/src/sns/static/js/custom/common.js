$('.dropdown .dropdown-menu li').click(function() {
  		/* Act on the event */
  		accountType = $(this).find('a').text();
  		$(this).parent().parent().find('.dropdown-toggle').text(accountType)
  		// this.parent('.dropdown-toggle').val(accountType)
  		 // console.info("asfasdf");
  	});
function AjaxPost(urlstr,jsonstr,callback) {
	// body...
	$.ajax({
      url: urlstr,
      type: 'POST',
      contentType:"application/json; charset=utf-8",  
      dataType: 'json',
      data: jsonstr,
    })
    .done(callback)
    .fail(function() {
      alert("error");
    });
}
function AjaxGet(urlstr,callback) {
  // body...
  $.ajax({
      url: urlstr,
      type: 'GET',
    })
    .done(callback)
    .fail(function() {
      alert("error");
    });
}