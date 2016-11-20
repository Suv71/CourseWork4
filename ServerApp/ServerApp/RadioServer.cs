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
    class RadioServer
    {
        private string _serverIpAdress;
        private int _port;
        private Socket _listenSocket;

        public RadioServer(string serverIpAdress, int port)
        {
            _serverIpAdress = serverIpAdress;
            _port = port;
        }

        public void Run()
        {
            IPEndPoint serverIpPoint = new IPEndPoint(IPAddress.Any, _port);
            _listenSocket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);

            try
            {
                _listenSocket.Bind(serverIpPoint);
                _listenSocket.Listen(10);

                Console.WriteLine("Сервер запущен. Ожидание подключений...");

                while (true)
                {
                    Socket connectionHandler = _listenSocket.Accept();

                    ClientHandler clientHandler = new ClientHandler(connectionHandler);

                    Thread clientThread = new Thread(new ThreadStart(clientHandler.Process));
                    clientThread.Start();
                    
                    //StringBuilder builder = new StringBuilder();
                    //int bytes = 0;
                    //byte[] data = new byte[256];
                    //int i = 1;

                    //while (!builder.ToString().Equals("End"))
                    //{
                    //    if (builder.Length != 0)
                    //    {
                    //        builder.Clear();
                    //    }

                    //    do
                    //    {
                    //        bytes = connectionHandler.Receive(data);
                    //        builder.Append(Encoding.ASCII.GetString(data, 0, bytes));
                    //    }
                    //    while (connectionHandler.Available > 0);

                    //    Console.WriteLine(DateTime.Now.ToShortTimeString() + ": " + builder.ToString());
                    //    string message = "Message received" + i++;
                    //    data = Encoding.ASCII.GetBytes(message);
                    //    connectionHandler.Send(data);

                    //connectionHandler.Close();
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
            }
            finally
            {
               // _listenSocket.Close();
            }
        }
    }
}
