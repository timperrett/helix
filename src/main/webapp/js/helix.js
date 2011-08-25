String.prototype.format = function(i, safe, arg) {
  function format() {
    var str = this, len = arguments.length+1;
    // For each {0} {1} {n...} replace with the argument in that position.  If 
    // the argument is an object or an array it will be stringified to JSON.
    for (i=0; i < len; arg = arguments[i++]) {
      safe = typeof arg === 'object' ? JSON.stringify(arg) : arg;
      str = str.replace(RegExp('\\{'+(i-1)+'\\}', 'g'), safe);
    }
    return str;
  }
  // Save a reference of what may already exist under the property native.  
  // Allows for doing something like: if("".format.native) { /* use native */ }
  format.native = String.prototype.format;
  // Replace the prototype property
  return format;
}();

$(function(){
  
  function toggleLoadingSpinner(){
    var icon = $('#loading')
    if(icon.is(':visible')){
      icon.hide();
    } else {
      icon.show();
    }
  }
  
  function isGithubUserRepoFormat(input){
    return /^([a-z0-9-]+)\/([a-z0-9-]+)$/.test(input)
  }
  
  function displaySourceUrlError(msg){
    var div = $('#sourceurl-wrapper');
    $('#sourceurl-error').text(msg)
    if(!div.hasClass('error')){
      div.addClass('error')
    }
  }
  
  function clearSourceUrlErrors(){
    var div = $('#sourceurl-wrapper');
    if(div.hasClass('error')){
      div.removeClass('error');
      $('#sourceurl-error').text('');
    }
  }
  
  $error = true;
  
  $('#sourceurl').typing({
    start: function(event, $elem){
      clearSourceUrlErrors();
    },
    stop: function(event, $elem){
      var input = $elem.val();
      if(isGithubUserRepoFormat(input)){
        toggleLoadingSpinner();
        var parts = input.split('/');
        gh.repo(parts[0], parts[1]).show(function(data){
          $error = false;
          $('#description').val(data.repository.description)
          $('#projectname').val(data.repository.name)
          $('#mainform').slideDown();
          $('button.primary').prop('disabled', false);
          clearSourceUrlErrors();
        });
        // nasty hack to work out if the github call
        // worked or not
        console.log($('#mainform').css('display'));
        if($error){
          displaySourceUrlError('Unable to find that repository on GitHub')
          $('#mainform').slideUp();
        }
        toggleLoadingSpinner();
      } else {
        // display error msg
        displaySourceUrlError('That is not a valid GitHub user/repo format')
      }
    },
    delay: 500
  });
  
});

