IFS.competitionManagement.inviteAssessorsFind = (function () {
  'use strict'
  return {
    init: function () {
      this.setInitialAddSelectedButtonState()
      this.onSelectAllCheckboxChange()
    },
    setInitialAddSelectedButtonState: function () {
      var $button = jQuery('[name="addSelected"]')
      var isInitiallyEnabled = !!$button.data('enableInitial')

      if (isInitiallyEnabled) {
        $button.removeAttr('aria-disabled').removeClass('disabled').prop('disabled', false)
      }
    },
    onSelectAllCheckboxChange: function () {
      var $selectedCounter = jQuery('[data-count-selected]')
      var maxCount = $selectedCounter.data('countSelectedMax')

      jQuery('[name="allSelected"]').on('change', function () {
        var $allSelectedCheckbox = jQuery(this)
        var isChecked = $allSelectedCheckbox.prop('checked')

        if (isChecked) {
          $selectedCounter.text(maxCount)
        } else {
          $selectedCounter.text(0)
        }
      })
    }
  }
})()
