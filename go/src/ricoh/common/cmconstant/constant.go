package cmconstant

const (
	//	CloudServerDomain = "http://ec2-52-197-33-153.ap-northeast-1.compute.amazonaws.com/"
	CloudServerDomain = "rocket2.hezhensh.com:80"
	//	CloudServerDomain = "172.25.78.117:80"
	PrinterDeviceType = "MFP"
	IwbDeviceType     = "IWB"
	PjsDeviceType     = "PJS"
	LocalServerId     = "id"

	FunctionPrint   = "print"
	FunctionProject = "project"

	// JobController used
	OkPrint        = "print job send succeed."
	OkProject      = "project succeed."
	OkIWB          = "iwb succeed."
	ErrNetwork     = "network error."
	ErrController  = "controller error."
	ErrInternal    = "internal error."
	ErrIWBAUTH     = "ErrIWBAUTH"
	ErrIWBSTARTED  = "ErrIWBSTARTED"
	ErrPJSSTARTED  = "ErrPJSSTARTED"
	ERRIWBCONNECT  = "ERRIWBCONNECT"
	ERRPJSCONNECT  = "ERRPJSCONNECT"
	ErrLocalServer = "ErrLocalServer"
	ErrJobType     = "can not know the job type(print or project)."
	// FileController used
	ErrDesType        = "can not know the destination type(user or channel)."
	LocalUnauthorized = 401
	LocalIsReplaced   = 402
)
