// Set up the handlers for adding and removing Cost Category costs rows
IFS.repeatableRows = (function() {
    "use strict";

    return {
        init: function(){
            jQuery('body').on('click', '[data-repeatable-rowcontainer]', function(e){  e.preventDefault(); IFS.repeatableRows.addRow(this,e); });
            jQuery('body').on('click', '.js-remove-row',function(e){ IFS.repeatableRows.removeRow(this,e); });
        },
        getAjaxUrl : function(el){
            var url = '';
            if(typeof(jQuery(el).val()) !== 'undefined' && typeof(jQuery(el).attr('name')) !== 'undefined' && jQuery("#application_id").length == 1){
                var applicationId =  jQuery("#application_id").val();
                url = window.location.protocol + '//'+window.location.host + '/application/' + applicationId + '/form/'+ jQuery(el).attr('name') + '/' + jQuery(el).val();
            }
            return url;
        },
        addRow : function(el,event){
            var url = IFS.repeatableRows.getAjaxUrl(el);
            if(url.length){
              event.preventDefault();
              jQuery.ajax({
                  url : url,
                  beforeSend : function(){
                      jQuery(el).before('<span class="form-hint">Adding a new row</span>');
                  }
              }).done(function(data){
                  var target = jQuery(el).attr("data-repeatable-rowcontainer");
                  jQuery(el).prev().remove();
                  jQuery(target).append(data);
                  jQuery('body').trigger('updateSerializedFormState');
              });
            }
        },
        removeRow : function(el,event){
            var url = IFS.repeatableRows.getAjaxUrl(el);
            if(url.length){
              event.preventDefault();
              jQuery.ajax({
                  url : url
              }).done(function(){
                  jQuery('[data-repeatable-row='+jQuery(el).val()+']').remove();
                  jQuery('body').trigger('recalculateAllFinances').trigger('updateSerializedFormState');
              });
            }
        }
        // generateFragmentUrl : function(originalLink) {
        //     // Replace the non-javascript add and remove functionality with single fragment question HTML responses
        //     var originalHref = originalLink.attr('href');
        //     var urlParamsParts = originalHref.split('?');
        //     var urlPart = urlParamsParts[0];
        //     var paramsPart = urlParamsParts.length == 2 ? ('&' + urlParamsParts[1]) : '';
        //
        //     var questionToUpdate = originalLink.closest('.question');
        //     var owningQuestionId;
        //     if( questionToUpdate.attr('id').indexOf('form-input-') !== -1){
        //         owningQuestionId = questionToUpdate.attr('id').replace('form-input-','');
        //     }
        //
        //     var dynamicHref = urlPart + '/' + owningQuestionId + '?singleFragment=true' + paramsPart;
        //     return dynamicHref;
        // },
        // addCostsRowHandler : function(e) {
        //     // Add a new row from the HTML fragment being returned, and bind / rebind the autocalc and autosave behaviours again to ensure it behaves as
        //     // per other fields and that the repeating-total fields have a back-reference to any added fields
        //     var amendRowsLink = jQuery(this);
        //     var dynamicHref = IFS.repeatableRows.generateFragmentUrl(amendRowsLink);
        //     var sectionToUpdate = amendRowsLink.closest('[data-repeatable-container]');
        //     var tableSectionId = sectionToUpdate.attr('data-repeatable-container');
        //
        //     jQuery.ajax({
        //         url : dynamicHref,
        //         beforeSend : function(){
        //             amendRowsLink.before('<span class="form-hint">Adding a new row</span>');
        //         }
        //     }).done(function(data){
        //         var htmlReplacement = jQuery('<div>' + data + '</div>');
        //         var replacement = htmlReplacement.find('[data-repeatable-container=' + tableSectionId + ']');
        //         sectionToUpdate.replaceWith(replacement);
        //         jQuery('body').trigger('updateSerializedFormState');
        //     });
        //
        //     e.preventDefault();
        //     return false;
        // },
        // removeCostsRowHandler : function(e) {
        //     var amendRowsLink = jQuery(this);
        //     var dynamicHref = IFS.repeatableRows.generateFragmentUrl(amendRowsLink);
        //
        //     jQuery.get(dynamicHref, function() {
        //         var costRowsId = amendRowsLink.attr('data-cost-row');
        //         var costRowsToDelete = jQuery('[data-cost-row=' + costRowsId + ']');
        //         var container = amendRowsLink.closest('[data-repeatable-container]');
        //         costRowsToDelete.remove();
        //
        //         container.find('[data-calculation-fields]').each(function(){
        //             var inst = jQuery(this);
        //             inst.attr({'data-calculation-rawvalue':0,'value':"£ 0"}).val("£ 0");
        //             var calculationFields = inst.attr('data-calculation-fields');
        //             jQuery(calculationFields).trigger('change');
        //         });
        //         jQuery('body').trigger('updateSerializedFormState');
        //     });
        //     e.preventDefault();
        //     return false;
        // }
    };
})();
