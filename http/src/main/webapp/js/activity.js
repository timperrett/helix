$(function(){
  $('.activity').hover(function(){
    var p = $(this).position();
    // determine the activity type
    var a = $(this).children('.activity-text').text();
    // set the activity title 
    $('#activity-title').text('Activity: '+a);
    // select the right activity description
    $('#activity-content').children('div').each(function(k,v){
      if(v.id == ('activity-'+a.toLowerCase())) $(v).css('display', 'block');
      else $(v).css('display', 'none');
    });
    // posistion and display
    $('.popover')
      .css('top', (p.top - 48) + 'px')
      .css('left', (p.left - 300) + 'px')
      .css('display', 'block');
  }, function(){
    if('.popover:visible') $('.popover').hide();
  });
});
