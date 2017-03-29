using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace ServerApp
{
    class Program
    {
        
        static void Main(string[] args)
        {
            String ipAdress;
            Console.WriteLine("Введите ip-адрес сервера:");
            ipAdress = Console.ReadLine();

            int port;
            Console.WriteLine("Введите порт сервера:");
            port = int.Parse(Console.ReadLine());

            //RadioServer server = new RadioServer("192.168.0.101", 31010);
            RadioServer server = new RadioServer(ipAdress, port);
            new Thread(new ThreadStart(server.Run)).Start();
        }
    }
}
