package slack;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

class WebSocketChatClient extends WebSocketClient {

	public WebSocketChatClient( URI serverUri ) {
		super( serverUri );
	}
	public WebSocketChatClient( URI serverUri ,Draft draft) {
		super( serverUri,draft);
	}

	@Override
	public void onOpen( ServerHandshake handshakedata ) {
		System.out.println( "Connected" );

	}

	@Override
	public void onMessage( String message ) {
		System.out.println( "got: " + message );

	}

	@Override
	public void onClose( int code, String reason, boolean remote ) {
		System.out.println( "Disconnected" );
//		System.exit( 0 );

	}

	@Override
	public void onError( Exception ex ) {
		ex.printStackTrace();

	}

}