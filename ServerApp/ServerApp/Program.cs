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
            RadioServer server = new RadioServer("192.168.0.101", 31010);
            new Thread(new ThreadStart(server.Run)).Start();
        }
    }
}
