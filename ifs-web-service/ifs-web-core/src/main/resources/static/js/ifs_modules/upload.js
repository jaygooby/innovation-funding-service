IFS.core.upload = (function () {
  'use strict'
  var s // private alias to settings
  return {
    settings: {
      uploadEl: '[type="file"][class="inputfile"]',
      wrapper: '.upload-section',
      downloadLocation: 'data-js-download-location',
      removeFileName: 'data-js-remove-file-name',
      uploadButtonName: 'data-js-upload-button-name',
      uploadFileInput: 'data-js-upload-file-input',
      singleUpload: 'data-js-single-upload',
      successRow: '<li class="success">' +
                    '<div class="file-row">' +
                      '<a href="$href" target="_blank">$text (Opens in a new window)</a>' +
                      '<button class="button-clear remove-file">Remove</button>' +
                    '</div>' +
                  '</li>',
      errorRow: '<li class="error">' +
                  '<div class="govuk-error-message">$error The file was not uploaded.</div>' +
                  '<div class="file-row">$text<button class="button-clear remove-file">Remove</button>' +
                  '</div>' +
                '</li>',
      pendingRow: '<li class="pending">' +
                    '<div class="file-row">$text' +
                      '<p class="saving">Uploading<span>.</span><span>.</span><span>.</span></p>' +
                    '</div>' +
                  '</li>',
      uploadView: '<input type="file" id="$id" name="$uploadFileInput" class="inputfile">' +
                  '<label for="$id" class="button-secondary govuk-!-margin-top-6">+ Upload</label>' +
                  '<button name="$uploadButtonName" class="button-secondary" data-for-file-upload="$id"></button>'
    },
    init: function () {
      s = this.settings
      if (typeof window.FormData !== 'undefined') {
        jQuery('body').on('change', s.uploadEl, function () {
          IFS.core.upload.ajaxFileUpload(jQuery(this))
        })
        jQuery('body').on('click', 'button.remove-file', function (e) {
          e.preventDefault()
          IFS.core.upload.removeFile(jQuery(this))
        })
      } else {
        jQuery('body').on('change', s.uploadEl, function () {
          IFS.core.upload.triggerFormSubmission(jQuery(this))
        })
      }
    },
    ajaxFileUpload: function (fileInput) {
      var wrapper = fileInput.closest(s.wrapper)
      var submitButton = IFS.core.upload.getButton(fileInput)
      var file = fileInput.get(0).files[0]

      IFS.core.upload.clearMessages(wrapper, 'li.error')

      var row = IFS.core.template.replaceInTemplate(s.pendingRow, {text: file.name})
      IFS.core.upload.addMessage(wrapper, row)

      var formData = new window.FormData()
      formData.append(fileInput.attr('name'), file)
      formData.append(submitButton.attr('name'), true)

      jQuery.ajaxProtected({
        type: 'POST',
        url: wrapper.closest('form').attr('action'),
        success: function (data) {
          IFS.core.upload.processAjaxResult(wrapper, file, data)
          IFS.core.upload.resetFileInput(wrapper)
        },
        error: function (error) {
          console.log(error)
          var errorMessage = ' Internal server error.'
          if (error.status === 413) {
            errorMessage = ' File is too large.'
          }
          IFS.core.upload.clearMessages(wrapper, 'li.pending')
          var row = IFS.core.template.replaceInTemplate(s.errorRow, {
            text: file.name,
            error: errorMessage
          })
          IFS.core.upload.addMessage(wrapper, row)
          IFS.core.upload.resetFileInput(wrapper)
        },
        processData: false,
        contentType: false,
        data: formData
      })
    },
    triggerFormSubmission: function (fileInput) {
      IFS.core.upload.getButton(fileInput).click()
    },
    getButton: function (fileInput) {
      var fileInputId = fileInput.attr('id')
      return jQuery('[data-for-file-upload="' + fileInputId + '"]')
    },
    processAjaxResult: function (wrapper, file, data) {
      var errorMessage = jQuery(data).find('ul.govuk-error-summary__list li a')
      IFS.core.upload.clearMessages(wrapper, 'li.pending')
      var row
      if (errorMessage.length) {
        row = IFS.core.template.replaceInTemplate(s.errorRow, {
          text: file.name,
          error: errorMessage.text()
        })
      } else {
        row = IFS.core.template.replaceInTemplate(s.successRow, {
          text: file.name,
          href: wrapper.attr(s.downloadLocation)
        })
      }
      IFS.core.upload.addMessage(wrapper, row)
      IFS.core.upload.toggleUploadView(wrapper, false)
    },
    getMessageList: function (wrapper) {
      var messageList = wrapper.find('ul.file-list')
      if (messageList.length) {
        return messageList
      }
      wrapper.find('input:file').before('<ul class="govuk-list file-list"></ul>')
      return wrapper.find('ul.file-list')
    },
    addMessage: function (wrapper, message) {
      var messageList = IFS.core.upload.getMessageList(wrapper)
      messageList.append(message)
      IFS.core.upload.toggleNoFileMessage(messageList)
    },
    clearMessages: function (wrapper, selector) {
      var messageList = IFS.core.upload.getMessageList(wrapper)
      messageList.find(selector).remove()
      IFS.core.upload.toggleNoFileMessage(messageList)
    },
    toggleNoFileMessage: function (messageList) {
      var noFileMessage = messageList.siblings('p:contains("No file currently uploaded.")')
      if (messageList.find('li').length) {
        noFileMessage.remove()
      } else {
        messageList.before('<p class="govuk-body uploaded-file">No file currently uploaded.</p>')
      }
    },
    toggleUploadView: function (wrapper, display) {
      if (display) {
        if (!wrapper.find('input:file').length) {
          var guid = IFS.core.template.guidGenerator()
          var html = IFS.core.template.replaceInTemplate(s.uploadView, {
            id: guid,
            uploadButtonName: wrapper.attr(s.uploadButtonName),
            uploadFileInput: wrapper.attr(s.uploadFileInput)
          })
          wrapper.append(html)
        }
      } else {
        wrapper.find('input:file, label, button[data-for-file-upload]')
          .remove()
      }
    },
    removeFile: function (removeButton) {
      var row = removeButton.closest('li')
      var wrapper = row.closest(s.wrapper)
      if (row.hasClass('success')) {
        var removeName = wrapper.attr(s.removeFileName)
        var formData = new window.FormData()
        formData.append(removeName, true)
        jQuery.ajaxProtected({
          type: 'POST',
          url: wrapper.closest('form').attr('action'),
          success: function (data) {
            IFS.core.upload.afterRemoveFile(row, wrapper)
          },
          error: function (error) {
            console.error(error)
          },
          processData: false,
          contentType: false,
          data: formData
        })
      } else {
        IFS.core.upload.afterRemoveFile(row, wrapper)
      }
    },
    afterRemoveFile: function (row, wrapper) {
      var messageList = IFS.core.upload.getMessageList(wrapper)
      row.remove()
      IFS.core.upload.toggleNoFileMessage(messageList)
      IFS.core.upload.toggleUploadView(wrapper, true)
    },
    resetFileInput: function (wrapper) {
      wrapper.wrap('<form>').closest('form').get(0).reset()
      wrapper.unwrap()
    }
  }
})()
