/* input value clear */
$(function(){
	$('.input_clear').each(function(){
		$(this).find('input').on('keyup focus', function(){
		   if($(this).val().length == 0){
			  $(this).removeClass('is_valued');
		   } else {
			  $(this).addClass('is_valued');
		   }
		});
  
		$(this).find('.clear_btn').on('click', function(){
		   $(this).closest('.input_clear').find('input').removeClass('is_valued').val('');
		   return false;
		});
	 });
});

/* input type toggle */
$(document).ready(function(){
	$('.pass_word i').on('click',function(){
		$('.pass_word input').toggleClass('active');
		if($('.pass_word input').hasClass('active')){
			$(this).attr('class',"eye_show").siblings('input').attr('type',"text");
		}else{
			$(this).attr('class',"eye_hide").siblings('input').attr('type','password');
		}
	}); 

	// $('.pass_word i').on('click',function(){
    //     $('.pass_word input').toggleClass('active');
    //     if($('.pass_word input').hasClass('active')){
    //         $(this).attr('class',"eye_show").parent().find('input').attr('type',"text");
    //     }else{
    //         $(this).attr('class',"eye_hide").parent().find('input').attr('type','password');
    //     }
    // });   
	
});
  
// accodion
// $(function() {
// 	// (Optional) Active an item if it has the class "is-active"
// 	var accordionState = true;
// 	var sccordionTimeout = null;
// 	$(".accodion > dt.is_active").next("dd").slideDown();
// 	 $('.accodion > dt input').click(function(e) {
// 		 accordionState = false;
// 	 });   
// 	$(".accodion > dt").click(function(e) {
// 	   var _this = this;
// 	   clearTimeout(sccordionTimeout);
// 	   sccordionTimeout = setTimeout(function(){
// 		  if (accordionState === true){
// 			 // Cancel the siblings
// 			 $(_this).siblings("dt").removeClass("is_active").next("dd").slideUp();
// 			 $(_this).attr('aria-expanded','true');
// 			 $(_this).siblings("dt").attr('aria-expanded','false');
			 
// 			 // Toggle the item
// 			 $(_this).toggleClass("is_active").next("dd").slideToggle("ease-out");
// 			 $(_this).not('.is_active').attr('aria-expanded','false');
// 			 accordionState = true;
// 		  } else {
// 			 accordionState = true;
// 		  }
// 	   }, 10)
// 	});
//  });

$(function() {
	setAccodion();
});

function setAccodion(){
	// (Optional) Active an item if it has the class "is-active"
	$(".accodion > dt.is_active").next("dd").slideDown();
	$(".accodion > dt").off("click").click(function(e) {
	   if ($(e.target).is('input') || $(e.target).is('span')) {
		  e.stopPropagation();
	   } else {
		  var _this = this;
		  // Cancel the siblings
		  $(_this).siblings("dt").removeClass("is_active").next("dd").slideUp();
		  $(_this).attr('aria-expanded','true');
		  $(_this).siblings("dt").attr('aria-expanded','false');
		  
		  // Toggle the item
		  $(_this).toggleClass("is_active").next("dd").slideToggle("ease-out");
		  $(_this).not('.is_active').attr('aria-expanded','false');
	   }
	});
}

    

 
// modal 
var pageScrTop = null;
function dimmVisible(){
	pageScrTop = $(window).scrollTop();
	$("html, body").addClass("is_dimmed").css('scroll-behavior', 'auto');
	$('body > div:first-child').css({position: 'relative', top: pageScrTop * (-1)});
}
//스크롤 활성화
function dimmHidden(){
	$("html, body").removeClass("is_dimmed");
	$('body > div:first-child').removeAttr('style');
	$(window).scrollTop(pageScrTop);
	$("html, body").removeAttr('style');
}

// Alert popup
function ModalAlertOpen(id){
	$(id).css({'z-index': '1300'});
	dimmVisible();
	$(id).addClass("is_visible is_active");
}
function ModalAlertClose(id){
	$(id).removeClass("is_active").one('transitionend', function(){
		if (!$(id).hasClass('is_active')) {
			$(id).removeClass("is_visible");

			// 남아있는 모달이 없는 경우 초기화
			if ($('.modal_wrap.is_visible').length == 0) {
				dimmHidden();
			}
		}
	})
}

// Dimmed popup
function ModalOpen(id){
	$(id).css({'z-index': '1300'});
	dimmVisible();
	$(id).addClass("is_visible is_active");
}
function ModalOpenClose(id){
	initializeScroll();
	$(id).removeClass("is_active").one('transitionend', function(){
		if (!$(id).hasClass('is_active')) {
			$(id).removeClass("is_visible");

			// 남아있는 모달이 없는 경우 초기화
			if ($('.modal_wrap.is_visible').length == 0) {
				dimmHidden();
			}
		}
	})
}

