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

        public RadioServer(string serverIpAdress, int port)
        {
            _serverIpAdress = serverIpAdress;
            _port = port;
            _clients = new List<ClientHandler>();
        }

        public void AddClient(ClientHandler client)
        {
            _clients.Add(client);
        }

        public void RemoveClient(int id)
        {
            _clients.Remove(_clients.FirstOrDefault(c => c.Id == id));
        }

        public void SendMessageToClient(int id, byte[] message)
        {
            ClientHandler client = _clients.FirstOrDefault(c => c.Id == id);
            client.SendMessage(message);
        }

        public void Run()
        {
            IPEndPoint serverIpPoint = new IPEndPoint(IPAddress.Parse(_serverIpAdress), _port);
            _listenSocket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);

            try
            {
                _listenSocket.Bind(serverIpPoint);
                _listenSocket.Listen(10);

                Console.WriteLine("Сервер запущен. Ожидание подключений...");

                while (true)
                {
                    Socket connectionHandler = _listenSocket.Accept();

                    ClientHandler clientHandler = new ClientHandler(connectionHandler, this);
                    Console.WriteLine("Новый клиент: " + clientHandler.Id);
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

        //public void DisconnectClients()
        //{
        //    foreach(var c in _clients)
        //    {
        //        c.
        //    }
        //}
    }
}
