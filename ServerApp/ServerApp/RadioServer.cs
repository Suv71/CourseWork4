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

        public List<ClientHandler> _clients;

        public RadioServer(string serverIpAdress, int port)
        {
            _serverIpAdress = serverIpAdress;
            _port = port;
            _listenSocket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            _clients = new List<ClientHandler>();
        }

        public void AddClient(ClientHandler client)
        {
            _clients.Add(client);
            /*Console.WriteLine("Id clients:");
            foreach(var c in _clients)
            {
                Console.WriteLine(c.Id);
            }*/
        }

        public void RemoveClient(int id)
        {
            _clients.Remove(_clients.FirstOrDefault(c => c.Id == id));
        }

        public void SendMessageToClient(int id, byte[] message)
        {
            ClientHandler client = _clients.FirstOrDefault(c => c.Id == id);
            Console.WriteLine("Отправляю этому = " + client.Id);
            client.SendMessage(message);
        }

        public byte[] GetActiveClients(int IdSender)
        {
            if (_clients.Count != 1)
            {
                int[] idClients = new int[_clients.Count - 1];
                int i = 0;
                foreach (var client in _clients)
                {
                    if (client.Id != IdSender)
                    {
                        idClients[i++] = client.Id;
                    }
                }

                byte[] res;
                res = BitConverter.GetBytes(idClients[0]);

                for (int j = 1; j < idClients.Length; j++)
                {
                    res = MergeArrays(res, BitConverter.GetBytes(idClients[j]));
                }

                return res;
            }
            else
            {
                return null;
            }
            
        }

        public int[] GetIdClients(byte[] clients)
        {
            if(clients != null)
            {
                int[] res = new int[clients.Length / 4];

                int j = 0;
                for (int i = 0; i < res.Length; i++)
                {
                    res[i] = BitConverter.ToInt32(clients, j);
                    j += 4;
                }

                return res;
            }
            else
            {
                return null;
            }
            
        }

        public byte[] MergeArrays(byte[] first, byte[] second)
        {
            byte[] res;
            if (first != null && second != null)
            {
                res = new byte[first.Length + second.Length];
                for (int i = 0; i < first.Length; i++)
                {
                    res[i] = first[i];
                }

                int j = first.Length;
                for (int i = 0; i < second.Length; i++)
                {
                    res[j++] = second[i];
                }

                return res;
            }
            else
            {
                return null;
            }

        }

        public void SendAllNewClient(int idFrom, byte[] nickname)
        {
            if(_clients.Count > 1)
            {
                SendBroadcastMessage(idFrom, BitConverter.GetBytes(Commands.newClient));
                SendBroadcastMessage(idFrom, BitConverter.GetBytes(nickname.Length));
                SendBroadcastMessage(idFrom, nickname);
            }
            else
            {
                Console.WriteLine("На сервере только один клиент");
            }
            
        }

        public void SendAllClientOut(int idFrom)
        {
            if (_clients.Count > 1)
            {
                SendBroadcastMessage(idFrom, BitConverter.GetBytes(Commands.clientOut));
                SendBroadcastMessage(idFrom, BitConverter.GetBytes(idFrom));
                //SendBroadcastMessage(idFrom, nickname);
            }
            else
            {
                Console.WriteLine("На сервере был только один клиент");
            }

        }

        public void SendBroadcastMessage(int idFrom, byte[] message)
        {
            for (int i = 0; i < _clients.Count; i++)
            {
                if (_clients[i].Id != idFrom) // если id клиента не равно id отправляющего
                {
                    _clients[i].SendMessage(message); //передача данных
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
    }
}
