package web

import (
	"github.com/astaxie/beego"
	"io"
	"mime/multipart"
	"os"
	"sns/common/snsglobal"
	"sns/common/snsstruct"
	"sns/controllers/snscommon"
	"sns/util/snslog"
	"strconv"
	"strings"
	"time"
)

type FileController struct {
	beego.Controller
}

func (this *FileController) Download() {
	filePath := this.GetString(":file")
	splits := strings.SplitN(filePath, "_", 2)
	snslog.D("Download/", filePath)
	if len(splits) != 2 {
		//	-----------------------------
		// responseOK.ErrDefine = snsglobal.SErrConfig.GetError("file", "file_not_exist")
		// responseOK.OK = false
		// this.Data["json"] = responseOK
		// this.ServeJSON()

		this.Ctx.Output.Download("files/" + filePath)
	} else {
		this.Ctx.Output.Download("files/"+filePath, splits[1])
	}

}
func (c *FileController) Upload() {
	r := c.Ctx.Request
	responseOK := snsstruct.FileGet{OK: true}
	r.ParseMultipartForm(32 << 20)
	var outfiles []snsstruct.File
	outfileNames := make(map[string]bool)
	var (
		file multipart.File
		err  error
	)
	if r.MultipartForm != nil && r.MultipartForm.File != nil {
		for _, aaa := range r.MultipartForm.File {
			for _, fileHeader := range aaa {
				if file, err = fileHeader.Open(); err != nil {
					snslog.E("open upload file fail:", fileHeader.Filename, err)
					continue
				}
				if _, ok := outfileNames[fileHeader.Filename]; ok {
					continue
				} else {
					outfileNames[fileHeader.Filename] = true
				}
				newName := strconv.FormatInt(time.Now().UnixNano(), 32) + "_" + fileHeader.Filename

				var tempPath = snscommon.Join(snscommon.GetFilePath("/"), newName)

				outfile, err_openFile := os.OpenFile(tempPath, os.O_WRONLY|os.O_CREATE, 0666) // 此处假设当前目录下已存在test目录
				if err_openFile != nil {
					snslog.E(err_openFile)
					responseOK.ErrDefine = snsglobal.SErrConfig.GetError("file", "upload_failed")
					c.Data["json"] = responseOK
					c.ServeJSON()
					return
				}

				io.Copy(outfile, file)
				file.Close()
				outfile.Close()
				outfiles = append(outfiles, snsstruct.File{Name: fileHeader.Filename, Url: newName})
				snslog.I("uploading ", fileHeader.Filename)
			}
		}
	}
	responseOK.Files = outfiles
	snslog.I("upload completed.")
	c.Data["json"] = responseOK
	c.ServeJSON()
}
