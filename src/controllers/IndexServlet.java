package controllers;

import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.Message;
import utils.DBUtil;

@WebServlet("/index")
public class IndexServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public IndexServlet() {
        super();
    }




    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        EntityManager em = DBUtil.createEntityManager();

        //開くページ数を取得、デフォルトは1ページ目
        int page = 1;
        try {
            page = Integer.parseInt(request.getParameter("page"));
        } catch(NumberFormatException e){}

        //開始位置（setFirstResult）と最大件数（setMaxResults）を指定してメッセージを取得
        List<Message> messages = em.createNamedQuery("getAllMessages", Message.class).setFirstResult((page-1)*15).setMaxResults(15).getResultList();

        //メッセージの全件数を取得
        long message_count = em.createNamedQuery("getMessagesCount", Long.class).getSingleResult().longValue();

        em.close();

        request.setAttribute("messages", messages);
        request.setAttribute("page", page);
        request.setAttribute("message_count", message_count);

        //フラッシュメッセージがセッションスコープに保存されていれば、
        //リクエストスコープに保存し、セッションスコープからは削除
        if (request.getSession().getAttribute("flush") != null) {
            request.setAttribute("flush",request.getSession().getAttribute("flush"));
            request.getSession().removeAttribute("flush");
        }

        RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/messages/index.jsp");
        rd.forward(request, response);
    }

}
