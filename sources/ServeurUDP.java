import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ServeurUDP {
	private final static int _dgLength = 50;
	private int _udpPort;
	private DatagramSocket dgSocket;
	private DatagramPacket dgPacket;
	private int nbJoueurs = 0;
	private String nomJoueur1=null;
	private String nomJoueur2=null;
	private InetAddress iPJoueur1=null;
	private InetAddress iPJoueur2=null;
	private int portJoueur1;
	private int portJoueur2;
	private int nbMancheJoueur1Win = 0;
	private int nbMancheJoueur2Win = 0;
	private int nbManche = 0;
	private String signeJoueur1 = null;
	private String signeJoueur2 = null;


	public ServeurUDP(int _udpPort) throws IOException {
		this._udpPort = _udpPort;
		dgSocket = new DatagramSocket(_udpPort);
	}

	private void go() throws IOException {

		while (true) {
			String msg = receive();
			String [] path = msg.split(":");

			if(nbJoueurs == 2 && nbManche != 0 ){
				if (msg.equals("PIERRE") || msg.equals("FEUILLE") || msg.equals("CISEAUX")){

					if (dgPacket.getAddress().equals(iPJoueur1) && signeJoueur1 == null){
						signeJoueur1= msg;
						portJoueur1 = dgPacket.getPort();
					}else if (dgPacket.getAddress().equals(iPJoueur2) && signeJoueur2 == null){
						signeJoueur2 = msg;
						portJoueur2 = dgPacket.getPort();
					}
					//-----------------------------------------------
					if (signeJoueur1 != null && signeJoueur2 != null)
					{
						System.out.println("Signe Joueur 1 : "+ signeJoueur1);
						System.out.println("Signe Joueur 2 : "+ signeJoueur2);
						if(nbManche == 1){
							if(nbMancheJoueur2Win > nbMancheJoueur1Win){
								send(whoWin()+"\nPARTIE FINIE\n"+nomJoueur2+" GAGNANT",iPJoueur1 , portJoueur1);
								send(whoWin()+"\nPARTIE FINIE\n"+nomJoueur2+" GAGNANT",iPJoueur2 , portJoueur2);
							}else if(nbMancheJoueur2Win < nbMancheJoueur1Win){
								send(whoWin()+"\nPARTIE FINIE\n"+nomJoueur1+" GAGNANT",iPJoueur1 , portJoueur1);
								send(whoWin()+"\nPARTIE FINIE\n"+nomJoueur1+" GAGNANT",iPJoueur2 , portJoueur2);
							}else{
								send(whoWin()+"\nPARTIE FINIE\n JOUEUR EXECAUX",iPJoueur1 , portJoueur1);
								send(whoWin()+"\nPARTIE FINIE\n JOUEUR EXECAUX",iPJoueur2 , portJoueur2);
							}
							nbManche--;
							reinitiate();
						}else{
							send(whoWin(),iPJoueur1 , portJoueur1);
							send(whoWin(),iPJoueur2 , portJoueur2);
							nbManche--;
						}
						signeJoueur1 = null;
						signeJoueur2 = null;
					}
				}else{
					send("ERROR(mouvement pas possible)",dgPacket.getAddress() , dgPacket.getPort());
				}
			}else{

				if(path[0].equals("JOIN")){
					if(path.length == 2){
						if(nomJoueur2 == null && nomJoueur1 != null){
							nomJoueur2 = path[1];
							iPJoueur2 = dgPacket.getAddress();
							portJoueur2 = dgPacket.getPort();
							nbJoueurs++;
							send("READY",iPJoueur2 , portJoueur2);
						}else{
								send("WAIT(pas de partie en cours)",dgPacket.getAddress() , dgPacket.getPort());
						}
					}else{
						send("ERROR(path invalide)",dgPacket.getAddress(), dgPacket.getPort());
					}
			}else if(path[0].equals("CREATE")){
					if(path.length == 3){
						if(Integer.parseInt(path[2])>0){
							if(nomJoueur1 == null){
								nomJoueur1 = path[1];
								nbManche = Integer.parseInt(path[2]);
								iPJoueur1 = dgPacket.getAddress();
								portJoueur1 = dgPacket.getPort();
								nbJoueurs++;
								send("READY(partie créée)",iPJoueur1, portJoueur1);
							}else{
								send("ERROR(partie déjà créée)",dgPacket.getAddress(), dgPacket.getPort());
							}
						}else{
							send("ERROR(nombres de manches invalides)",dgPacket.getAddress(), dgPacket.getPort());
						}
						}else{
							send("ERROR(path invalide)",dgPacket.getAddress(), dgPacket.getPort());
						}
		}else{
			send("ERROR(path invalide)",dgPacket.getAddress(), dgPacket.getPort());
		}
	}
		}

	}

	private String whoWin()
		{
			if (signeJoueur1.equals("CISEAUX") && signeJoueur2.equals("PIERRE")){
				nbMancheJoueur2Win++;
				return nomJoueur2+" WIN";
			}else if(signeJoueur1.equals("PIERRE") && signeJoueur2.equals("CISEAUX")){
				nbMancheJoueur1Win++;
				return nomJoueur1+" WIN";
			}else if(signeJoueur1.equals("PIERRE") && signeJoueur2.equals("FEUILLE")){
				nbMancheJoueur2Win++;
				return nomJoueur2+" WIN";
			}else if(signeJoueur1.equals("FEUILLE") && signeJoueur2.equals("PIERRE")){
				nbMancheJoueur1Win++;
				return nomJoueur1+" WIN";
			}else if(signeJoueur1.equals("FEUILLE") && signeJoueur2.equals("CISEAUX")){
				nbMancheJoueur2Win++;
				return nomJoueur2+" WIN";
			}else if(signeJoueur1.equals("CISEAUX") && signeJoueur2.equals("FEUILLE")){
				nbMancheJoueur1Win++;
				return nomJoueur1+" WIN";
			}else{
				return "EXECAUX";
			}
	}
	private String receive() throws IOException {
		byte[] buffer = new byte[_dgLength];
		dgPacket = new DatagramPacket(buffer, _dgLength);
		dgSocket.receive(dgPacket);
		return new String(dgPacket.getData(), dgPacket.getOffset(), dgPacket.getLength());

	}

	private void reinitiate(){
		nomJoueur1 = null;
		nomJoueur2 = null;
		iPJoueur1 = null;
		iPJoueur2 = null;
		nbJoueurs = 0;
		signeJoueur1 = null;
		signeJoueur2 = null;
		nbMancheJoueur2Win = 0;
		nbMancheJoueur1Win = 0;
	}

	private void send(String msg, InetAddress address, int port) throws IOException {
		byte[] buffer = msg.getBytes();
		dgPacket = new DatagramPacket(buffer, 0, buffer.length);
		dgPacket.setAddress(address);
		dgPacket.setPort(port);
		dgSocket.send(dgPacket);
	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		ServeurUDP serveur = new ServeurUDP(8080);
		serveur.go();
	}

}
