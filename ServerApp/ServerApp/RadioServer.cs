using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace ServerApp
{
    public class RadioServer
    {
        private string _serverIpAdress;
        private int _port;
        private Socket _listenSocket;

        private List<ClientHandler> _clients;
        private int clientsInChat;

        public RadioServer(string serverIpAdress, int port)
        {
            _serverIpAdress = serverIpAdress;
            _port = port;
            _listenSocket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            _clients = new List<ClientHandler>();
            clientsInChat = 0;
        }

        public void AddClient(ClientHandler client)
        {
            _clients.Add(client);
        }

        public void RemoveClient(int id)
        {
            _clients.Remove(_clients.FirstOrDefault(c => c.Id == id));
        }

        public void AddClientInChat()
        {
            clientsInChat += 1;
        }

        public void RemoveClientFromChat()
        {
            clientsInChat -= 1;
        }

        private String[] GetClientsInChat(String nickname)
        {
            if (clientsInChat > 1)
            {
                String[] clients = new String[_clients.Count - 1];
                int i = 0;
                foreach (var client in _clients)
                {
                    if (client.Nickname != nickname)
                    {
                        clients[i++] = client.Nickname;
                    }
                }

                return clients;
            }
            else
            {
                return null;
            }
        }

        public IReadOnlyList<ClientHandler> GetClients()
        {
            return _clients.AsReadOnly();
        }

        public void SendMessageToClient(String nickname, byte[] message)
        {
            ClientHandler client = _clients.FirstOrDefault(c => c.Nickname == nickname);
            client.SendMessage(message);
        }

        

        public void SendActiveClients(String nickname)
        {
            String[] clients = GetClientsInChat(nickname);

            if(clients != null)
            {
                SendMessageToClient(nickname, BitConverter.GetBytes(Commands.activeClients));
                SendMessageToClient(nickname, BitConverter.GetBytes(clients.Length));

                for (int i = 0; i < clients.Length; i++)
                {
                    SendMessageToClient(nickname, BitConverter.GetBytes(clients[i].Length));
                    SendMessageToClient(nickname, Encoding.UTF8.GetBytes(clients[i]));
                }
            }
            else
            {
                //Console.WriteLine("На сервере только один пользователь");
            } 
        }
  
        public void SendAllNewClient(int idSender, byte[] nickname)
        {
            if(_clients.Count > 1)
            {
                SendBroadcastMessage(idSender, BitConverter.GetBytes(Commands.newClient));
                SendBroadcastMessage(idSender, BitConverter.GetBytes(nickname.Length));
                SendBroadcastMessage(idSender, nickname);
            }
            else
            {
                Console.WriteLine("На сервере только один пользователь");
            }
            
        }

        public void SendAllClientOut(int idSender, byte[] nickname)
        {
            if (_clients.Count > 1)
            {
                SendBroadcastMessage(idSender, BitConverter.GetBytes(Commands.clientOut));
                SendBroadcastMessage(idSender, BitConverter.GetBytes(nickname.Length));
                SendBroadcastMessage(idSender, nickname);
            }
            else
            {
                Console.WriteLine("На сервере был только один клиент");
            }

        }

        public void SendBroadcastMessage(int idSender, byte[] message)
        {
            for (int i = 0; i < _clients.Count; i++)
            {
                if (_clients[i].Id != idSender) 
                {
                    _clients[i].SendMessage(message);
                }
            }
        }

        public void Run()
        {
            IPEndPoint serverIpPoint = new IPEndPoint(IPAddress.Parse(_serverIpAdress), _port);

            try
            {
                _listenSocket.Bind(serverIpPoint);
                _listenSocket.Listen(10);

                Console.WriteLine("Сервер запущен. Ожидание подключений...");

                while (true)
                {
                    Socket connectionHandler = _listenSocket.Accept();

                    ClientHandler clientHandler = new ClientHandler(connectionHandler, this);
                    Console.WriteLine(new StringBuilder("Клиент с id = " + clientHandler.Id + " подключился к серверу").ToString());
                    new Thread(new ThreadStart(clientHandler.Process)).Start();
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
            }
            finally
            {
                _listenSocket.Close();
            }
        }
    }
}
