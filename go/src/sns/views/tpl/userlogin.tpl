 
<script src="/static/js/custom/common.js"></script>
<script type="text/javascript">
  $('#submit').click(function(event) {
    /* Act on the event */
    emailAccount = $('#email').val()
    password = $('#pwd').val()
    var valid = checkInput(emailAccount,password);
    if (valid==true){
      AjaxPost('/user/login'
        , JSON.stringify({'email': emailAccount, 'password':password})
        ,function(data){
          if (data.Ok==true){
              alert("success")
              window.location.replace("/user");
          }else{
            alert("login fail");
          }
          // data = JSON.parse(data);
          // window.location.replace(data.url)
        }
      );
    }
  });
  function checkInput(email,pwd){
    return true;
  }
</script>