using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
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

                    StringBuilder builder = new StringBuilder();
                    int bytes = 0;
                    byte[] data = new byte[256];

                    do
                    {
                        bytes = connectionHandler.Receive(data);
                        builder.Append(Encoding.Unicode.GetString(data, 0, bytes));
                    }
                    while (connectionHandler.Available > 0);

                    Console.WriteLine(DateTime.Now.ToShortTimeString() + ": " + builder.ToString());

                    string message = "Сообщение доставлено";
                    data = Encoding.Unicode.GetBytes(message);
                    connectionHandler.Send(data);

                    connectionHandler.Shutdown(SocketShutdown.Both);
                    connectionHandler.Close();
                }

            }
            catch(Exception ex)
            {
                Console.WriteLine(ex.Message);
            }
        }
    }
}
